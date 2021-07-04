package com.example.ketxe

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

//https://medium.com/@jayd1992/foreground-services-in-android-e131a863a33d
//https://github.com/jeetdholakia/ServicesAndBackgroundTasks/blob/master/app/src/main/java/dev/jeetdholakia/servicesandbackgroundtasks/foregroundservices/MyForegroundService.kt

private val TAG = "com.example.ketxe.view.home.RealmDBService"

fun log(msg: String) {
    Log.w(TAG, msg)
}

class MyJobService: Service() {
    var onJob = false

    @RequiresApi(Build.VERSION_CODES.O)
    private fun job() {
        while (onJob) {
            log("===============================")
            val job = BackgroundJob(this)
            job.run()
            Thread.sleep(Configuration.backgroundPeriod)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = createNotificationChannel(this, "channelID", "channelName")
        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.s_ico)
            .setContentTitle("Hệ thống cảnh báo kẹt xe")
            .setContentText("... App đang cập nhật dữ liệu giao thông 24/24 ...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        if (!onJob) {
            Thread { job() }.start()
            onJob = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onJob = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startJob(context: Context) {
            val myServiceIntent = Intent(context, MyJobService::class.java)
            ContextCompat.startForegroundService(context, myServiceIntent)
        }
    }
}

//https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannel(context: Context, channelId: String, channelName: String): String {
    val chan = NotificationChannel(channelId,
        channelName, NotificationManager.IMPORTANCE_NONE)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
}