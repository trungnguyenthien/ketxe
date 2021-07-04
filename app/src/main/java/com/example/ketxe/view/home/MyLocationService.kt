package com.example.ketxe.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

interface MyLocationService {
    fun startRequest(activity: Activity, onStart: () -> Unit, onStop: () -> Unit, onSuccess: (Location) -> Unit)
    fun stopRequest()
}

class FusedLocationService : MyLocationService {
    private var onStopHandler: (() -> Unit)? = null
    private var onSuccessHandler: ((Location) -> Unit)? = null
    private var activity: Activity? = null
    private var callback: LocationCallback? = null

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy { FusedLocationProviderClient(activity!!) }

    private fun start() {
        val noPermission = dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (noPermission) openLocationSettingScreen()
        else requestMyLocation()
    }

    private fun openLocationSettingScreen() {
        activity?.run {
            AlertDialog.Builder(this)
                .setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, which ->
                    this.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    stopRequest()
                }.setNegativeButton("No") { dialog, which ->
                    dialog.cancel()
                    stopRequest()
                }.create().show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestMyLocation() {
        if (dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            stopRequest()
            return
        }

        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        request.interval = 100
        request.fastestInterval = 100
        request.smallestDisplacement = 1F

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result?.let { onSuccessHandler?.invoke(it.locations.last()) }
                stopRequest()
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(request, callback, null)
    }

    override fun startRequest(activity: Activity, onStart: () -> Unit, onStop: () -> Unit, onSuccess: (Location) -> Unit) {
        this.activity = activity
        onStopHandler = onStop
        onSuccessHandler = onSuccess
        onStart.invoke()
        start()
    }

    override fun stopRequest() {
        callback?.let { fusedLocationProviderClient.removeLocationUpdates(it) }
        onStopHandler?.invoke()
        resetVar()
    }

    private fun resetVar() {
        activity = null
        onStopHandler = null
        onSuccessHandler = null
        callback = null
    }

    private fun dontGrant(permission: String): Boolean =
        activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED
}