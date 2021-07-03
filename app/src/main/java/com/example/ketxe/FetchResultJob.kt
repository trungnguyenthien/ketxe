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
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*


fun connectDB(): RealmDBService? {
    Realm.getDefaultInstance()?.run { return RealmDBService(this) }
    return null
}

class FetchResultJob(val context: Context) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun run() {
        connectDB()?.let { fetchAllAddress(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAllAddress(db: RealmDBService) {
        db.getAllAddress().forEach { address ->
            processForEach(address)
        }
    }

    private val api = TrafficBingServiceImpl()
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processForEach(address: Address) {
        val ll = LatLng(address.lat.toDouble(), address.lon.toDouble())
        api.request(ll, radius = 5.0, completion = { resources ->
            save(address, resources)
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun save(address: Address, resources: List<Resources>) {
        connectDB()?.let { db ->
            val addressId = address.id ?: ""
            db.deleteStuck(addressId = addressId, completion = {
                val newStucks = resources.filter {
                    val startTime = toDate(it.start)
                    val now = Date()
                    val isToday = startTime.simpleDateFormat() == now.simpleDateFormat()
                    val isDelay1hour = (now.HOUR_OF_DAY() - startTime.HOUR_OF_DAY()) < 1
                    val verified = it.verified
                    return@filter verified && isToday && isDelay1hour
                }.map { Stuck(
                    id = null,
                    addressId = addressId,
                    description = it.description,
                    latitude = it.toPoint.coordinates[0].toFloat(),
                    longitude = it.toPoint.coordinates[1].toFloat(),
                    updateTime = Date(),
                    severity = stuckSeverity(code = it.severity),
                    startTime = toDate(it.start),
                    fromPoint = it.point.coordinates.joinToString(","),
                    toPoint = it.toPoint.coordinates.joinToString(",")
                )}
                db.saveStuck(addressId = addressId, stucks = newStucks, completion = {
                    notifyStuck(address = address, stucks = newStucks)
                })
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notifyStuck(address: Address, stucks: List<Stuck>) {
        val numberSerious = stucks.filter { it.severity == StuckSeverity.Serious }.size
        val numberModerate = stucks.filter { it.severity == StuckSeverity.Moderate }.size
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibratePattern = longArrayOf(0, 250, 100, 250)
        val mBuilder = NotificationCompat.Builder(context, "channelID")
            .setSmallIcon(R.drawable.image_address_map_icon) // notification icon
            .setContentTitle("🔴 Khu vực [${address.description}]") // title for notification
            .setContentText("Có $numberSerious điểm kẹt xe, $numberModerate điểm đông xe") // message for notification
            .setAutoCancel(true) // clear notification after click
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVibrate(vibratePattern)
            .setSound(alarmSound)
            .setGroup("ketxe")
//            .setStyle(NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line...Much longer text that cannot fit one line...Much longer text that cannot fit one line...Much longer text that cannot fit one line..."))

        val intent = Intent(context, MapsActivity::class.java).apply {
            this.putExtra("address", address.id)
            this.putExtra("lat", address.lat)
            this.putExtra("lon", address.lon)
        }
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pi)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.notify(address.description.length, mBuilder.build())
    }
}

fun toDate(stringDate: String): Date {
    // /Date(1625282722636)/
    val stringTime = stringDate
        .replace("/Date(","")
        .replace(")/","")
    val longValue = stringTime.toLong()
    return Date(longValue)
}

fun Date.simpleDateFormat(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    return sdf.format(Date())
}

fun Date.detailDateFormat(): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm")
    return sdf.format(Date())
}

fun Date.HOUR_OF_DAY(): Int {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.HOUR_OF_DAY)
}

fun Date.MINUTE(): Int {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.MINUTE)
}

fun Date.SECOND(): Int {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.SECOND)
}