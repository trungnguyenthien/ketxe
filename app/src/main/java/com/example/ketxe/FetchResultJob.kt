package com.example.ketxe

import android.R
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.ketxe.entity.Resources
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm
import java.util.*


fun connectDB(): RealmDBService? {
    Realm.getDefaultInstance()?.run { return RealmDBService(this) }
    return null
}

class FetchResultJob(val context: Context) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun run() {
        connectDB()?.let { db ->
            fetchAllAddress(db)
            db.printPreviousLog()
            db.saveLog("---")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAllAddress(db: RealmDBService) {
        db.getAllAddress { list ->
            log("will fetchList size = ${list.size}")
            list.forEach { address ->
                log("will fetch address = ${address.description}")
            processForEach(address, completion = {
                log("Fetch address = ${address.description}")
            })
        }}
    }
    private val api = TrafficBingServiceImpl()
    @RequiresApi(Build.VERSION_CODES.O)
    private fun processForEach(address: Address, completion: (List<Resources>) -> Unit) {

        val ll = LatLng(address.lat.toDouble(), address.lon.toDouble())
        api.request(ll, radius = 5.0, completion = { resources ->
            save(address, resources)
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun save(address: Address, resources: List<Resources>) {
        connectDB()?.let { db ->
            val addressId = address.id ?: ""
            val newStucks = resources.map { Stuck(
                id = null,
                addressId = addressId,
                description = it.description,
                latitude = it.toPoint.coordinates[0].toFloat(),
                longitude = it.toPoint.coordinates[1].toFloat(),
                updateTime = Date(),
                severity = stuckSeverity(code = it.severity)
            )}
            db.saveStuck(addressId = addressId, stucks = newStucks, completion = {
                notifyStuck(address = address, stucks = newStucks)
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notifyStuck(address: Address, stucks: List<Stuck>) {
        val numberSerious = stucks.filter { it.severity == StuckSeverity.Serious }.size
        val numberModerate = stucks.filter { it.severity == StuckSeverity.Moderate }.size

        val mBuilder = NotificationCompat.Builder(context, "channelID")
            .setSmallIcon(com.example.ketxe.R.drawable.image_address_map_icon) // notification icon
            .setContentTitle("🔴 Khu vực [${address.description}]") // title for notification
            .setContentText("Có $numberSerious điểm kẹt xe, $numberModerate điểm đông xe") // message for notification
            .setAutoCancel(true) // clear notification after click
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(0xf44).setColorized(true)
            .setCategory(Notification.CATEGORY_SERVICE)
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