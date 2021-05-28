package com.example.ketxe


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


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

fun requestTrafficPermission(activity: Activity, code: Int) {
    PermissionRequester(
        activity = activity,
        permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        requestCode = code
    ).requestIfNeed()
}