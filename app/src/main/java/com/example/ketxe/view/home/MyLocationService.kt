package com.example.ketxe.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

interface MyLocationService {
    fun request(activity: Activity, onStart: () -> Unit, onStop: () -> Unit, onSuccess: (Location) -> Unit)
    fun stopRequest()
}

class MyLocationRequester: LocationListener, MyLocationService {
    private var locationManager: LocationManager? = null
    private var onStopHandler: (() -> Unit)? = null
    private var onSuccessHandler: ((Location) -> Unit)? = null
    private var activity: Activity? = null

    override fun onLocationChanged(location: Location) {
        onSuccessHandler?.invoke(location)
        locationManager?.removeUpdates(this)
        onStopHandler?.invoke()
    }

    private fun start() {
        val enableProvider = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (enableProvider == true) requestMyLocation()
        else requestLocationSetting()
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

    @SuppressLint("MissingPermission")
    private fun requestMyLocation() {
        if (dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return
        }
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun request(
        activity: Activity,
        onStart: () -> Unit,
        onStop: () -> Unit,
        onSuccess: (Location) -> Unit
    ) {
        this.activity = activity
        locationManager = activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        onStopHandler = onStop
        onSuccessHandler = onSuccess
        onStart.invoke()
        start()
    }

    override fun stopRequest() {
        locationManager?.removeUpdates(this)
        onStopHandler?.invoke()
    }
}