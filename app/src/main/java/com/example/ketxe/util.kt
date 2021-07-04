package com.example.ketxe


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*


class PermissionRequester(
    val activity: Activity,
    val permissions: Array<String>,
    val requestCode: Int) {

    fun requestIfNeed() {
        val grantees = permissions.map { isGranted(it) }
        val isNoPermission = grantees.contains(false)
        if (isNoPermission) startRequest()
    }

    private fun startRequest() {
        ActivityCompat.requestPermissions(activity, permissions, requestCode )
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
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
