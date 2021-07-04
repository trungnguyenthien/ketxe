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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

//https://medium.com/@jayd1992/foreground-services-in-android-e131a863a33d
//https://github.com/jeetdholakia/ServicesAndBackgroundTasks/blob/master/app/src/main/java/dev/jeetdholakia/servicesandbackgroundtasks/foregroundservices/MyForegroundService.kt
class MyForegroundService: Service() {
    companion object {
        fun start(context: Context) {
            val myServiceIntent = Intent(context, MyForegroundService::class.java)
            myServiceIntent.action = "START"
            ContextCompat.startForegroundService(context, myServiceIntent)
        }

        fun stop(context: Context) {
            val myServiceIntent = Intent(context, MyForegroundService::class.java)
            myServiceIntent.action = "STOP"
            ContextCompat.startForegroundService(context, myServiceIntent)
        }
    }

    private var onJob = false

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
        return if(intent?.action == "START") {
            KeyValueStorage(this).setIsBackgroundServiceRunning(true)
            onStart()
        } else {
            KeyValueStorage(this).setIsBackgroundServiceRunning(false)
            onStop(startId)
        }
    }

    private fun onStart(): Int {
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
        return START_STICKY
    }

    private fun onStop(startId: Int): Int {
        stopForeground(true);
        stopSelfResult(startId);
        return START_NOT_STICKY
    }

    private fun clearAllNotification() {
        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        if (onJob) return
        Thread { job() }.start()
        onJob = true
        KeyValueStorage(this).setIsBackgroundServiceRunning(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        onJob = false
        clearAllNotification()
        KeyValueStorage(this).setIsBackgroundServiceRunning(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null
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