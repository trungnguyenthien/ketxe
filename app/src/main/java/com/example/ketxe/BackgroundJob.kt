package com.example.ketxe

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ketxe.entity.Resources
import com.example.ketxe.entity.UserIncident
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import java.util.*


class BackgroundJob(val context: Context) {
    private val dbService: DataService = RealmDBService()
    private val api: TrafficService = TrafficBingService()
    private val channelID = "channelID"

    private var willSound = false
    @RequiresApi(Build.VERSION_CODES.O)
    fun run() { // <= Function này sẽ run mỗi lần thực hiện background job.
//        clearAllNotification()
        willSound = false

        dbService.getAllAddress().filter { it.inTime() }.forEach { address ->
            process(address) { willSound = willSound || it }
        }

        Handler(Looper.getMainLooper()).postDelayed( {
            if (willSound) playSound()
            willSound = false
        }, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun process(address: Address, callback: (Boolean) -> Unit) {
        val ll = LatLng(address.lat.toDouble(), address.lng.toDouble())
        api.request(ll, radius = 2.0, completion = { resources, userIncidents ->
            updateStucksInDB(address, resources, userIncidents, completion = { address, newStucks ->
                showNotification(address, newStucks)
                callback.invoke(allowPlaySound(newStucks))
            })
        })
    }

    private fun allowPlaySound(stucks: List<Stuck>): Boolean {
        val result = analyse(stucks)
        return (result.closesRoadsCount + result.seriousCount + result.noSeriousCount) > 0
    }

    private fun playSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ring = RingtoneManager.getRingtone(context, notification)
            ring.volume = 0.7F
            ring.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStucksInDB(address: Address, resources: List<Resources>, uincidents: List<UserIncident>, completion: (Address, List<Stuck>) -> Unit) {
        val addressId = address.id ?: ""
        dbService.delete(addressId = addressId, completion = {
            val newStucks = resources.filter {
                val startTime = toDate(it.start)
                val now = Date()
//                val isToday = startTime.simpleDateFormat() == now.simpleDateFormat()
                return@filter it.verified // && isToday
            }.map {
                Stuck(
                    id = null,
                    addressId = addressId,
                    description = it.description,
                    latitude = it.toPoint.coordinates[0].toFloat(),
                    longitude = it.toPoint.coordinates[1].toFloat(),
                    updateTime = Date(),
                    severity = stuckSeverity(code = it.severity),
                    startTime = toDate(it.start),
                    fromPoint = it.point.coordinates.joinToString(","),
                    toPoint = it.toPoint.coordinates.joinToString(","),
                    isClosedRoad = it.roadClosed,
                    type = stuckType(it.type),
                    title = it.title ?: "---"
                )
            }

            dbService.save(addressId = addressId, stucks = newStucks, uincidents = uincidents, completion = {
                completion.invoke(address, newStucks)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(address: Address, stucks: List<Stuck>) {
        val result = analyse(stucks)
        val vibratePattern = longArrayOf(0, 250, 100, 250)

        val mBuilder = NotificationCompat.Builder(context, channelID)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setSmallIcon(R.drawable.image_address_map_icon) // notification icon
            .setContentTitle("🔴 Khu vực [${address.description}]") // title for notification
            .setContentText(makeMessage(result)) // message for notification
            .setAutoCancel(true) // clear notification after click
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVibrate(vibratePattern)
            .setGroup("ketxe")
            .setContentIntent(makeIntent(address))
            .setStyle(NotificationCompat.BigTextStyle().bigText(makeMessage(result)))

        notificationManager?.notify(address.description.length, mBuilder.build())
    }

//    private fun clearAllNotification() {
//        notificationManager?.deleteNotificationChannel(channelID)
//    }

    private val notificationManager get() =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

    private fun makeMessage(result: AnalyseResult): String {
        return "${result.closesRoadsCount} đường bị chặn, " +
                "${result.seriousCount} điểm kẹt xe, " +
                "${result.noSeriousCount} điểm đông xe"
    }

    private fun makeIntent(address: Address): PendingIntent {
        val intent = Intent(context, MapsActivity::class.java).apply {
            this.putExtra("address", address.id)
            this.putExtra("lat", address.lat)
            this.putExtra("lng", address.lng)
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}