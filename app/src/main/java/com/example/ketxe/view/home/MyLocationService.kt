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
    fun request(activity: Activity, onStart: () -> Unit, onStop: () -> Unit, onSuccess: (Location) -> Unit)
    fun stopRequest()
}

class MyLocationRequester(): MyLocationService {
    private var onStopHandler: (() -> Unit)? = null
    private var onSuccessHandler: ((Location) -> Unit)? = null
    private var activity: Activity? = null
    val fusedLocationProviderClient: FusedLocationProviderClient by lazy { FusedLocationProviderClient(activity!!) }

    private fun start() {
        val noPermission = dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (noPermission) requestLocationSetting()
        else requestMyLocation()
    }

    private fun requestLocationSetting() {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, which ->
                    activity!!.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton("No") { dialog, which ->
                    dialog.cancel()
                }.create().show()
        }
    }

    private fun dontGrant(permission: String): Boolean =
        activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    private fun requestMyLocation() {
        if (dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return
        }

        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 100
        request.fastestInterval = 100
        request.smallestDisplacement = 1F

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result?.run {
                    onSuccessHandler?.invoke(result.locations.last())
                    onStopHandler?.invoke()
                    fusedLocationProviderClient.removeLocationUpdates(callback)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(request, callback, null)
    }

    override fun request(activity: Activity, onStart: () -> Unit, onStop: () -> Unit, onSuccess: (Location) -> Unit) {
        this.activity = activity
        onStopHandler = onStop
        onSuccessHandler = onSuccess
        onStart.invoke()
        start()
    }

    override fun stopRequest() {
        callback?.run {
            fusedLocationProviderClient.removeLocationUpdates(callback)
            onStopHandler?.invoke()
        }
    }
}