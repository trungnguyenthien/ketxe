package com.example.ketxe

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ketxe.entity.Resources
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

class BackgroundJob(val context: Context) {
    private val dbService: DataService = RealmDBService()
    private val api: TrafficService = TrafficBingService()

    @RequiresApi(Build.VERSION_CODES.O)
    fun run() {
        dbService.getAllAddress().forEach { address ->
            process(address)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun process(address: Address) {
        val ll = LatLng(address.lat.toDouble(), address.lng.toDouble())
        api.request(ll, radius = 5.0, completion = { resources ->
            updateStucksInDB(address, resources, completion = { address, newStucks ->
                showNotification(address, newStucks)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStucksInDB(address: Address, resources: List<Resources>, completion: (Address, List<Stuck>) -> Unit) {
        val addressId = address.id ?: ""
        dbService.deleteStuck(addressId = addressId, completion = {
            val newStucks = resources.filter {
                val startTime = toDate(it.start)
                val now = Date()
                val isToday = startTime.simpleDateFormat() == now.simpleDateFormat()
                val verified = it.verified
                return@filter verified && isToday
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

            dbService.saveStuck(addressId = addressId, stucks = newStucks, completion = {
                completion.invoke(address, newStucks)
            })
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(address: Address, stucks: List<Stuck>) {
        val result = analyse(stucks)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibratePattern = longArrayOf(0, 250, 100, 250)

        val mBuilder = NotificationCompat.Builder(context, "channelID")
            .setSmallIcon(R.drawable.image_address_map_icon) // notification icon
            .setContentTitle("🔴 Khu vực [${address.description}]") // title for notification
            .setContentText(makeMessage(result)) // message for notification
            .setAutoCancel(true) // clear notification after click
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVibrate(vibratePattern)
            .setSound(alarmSound)
            .setGroup("ketxe")
            .setContentIntent(makeIntent(address))
            .setStyle(NotificationCompat.BigTextStyle().bigText(makeMessage(result)))

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.notify(address.description.length, mBuilder.build())
    }

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