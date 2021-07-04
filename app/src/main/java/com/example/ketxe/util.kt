package com.example.ketxe


import android.app.Activity
import android.app.ActivityManager
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
    return sdf.format(this)
}