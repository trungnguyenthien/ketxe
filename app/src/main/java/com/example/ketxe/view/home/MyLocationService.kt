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
    fun request(activity: Activity, listener: MyLocationRequester.Listener?)
}

class MyLocationServiceImpl(activity: Activity): MyLocationService {
    private val myLocRequester = MyLocationRequester(activity)
    override fun request(activity: Activity, listener: MyLocationRequester.Listener?) {
        myLocRequester.startRequest(listener)
    }
}

class MyLocationRequester(private val activity: Activity): LocationListener {
    private val locationManager: LocationManager by lazy {
        activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
    }

    interface Listener {
        fun onStartGetMyLocation()
        fun onStopGetMyLocation()
        fun onSuccessGetMyLocation(location: Location)
    }

    private var listener: Listener? = null

    fun startRequest(listener: Listener? = null) {
        this.listener = listener
        listener?.onStartGetMyLocation()
        start()
    }

    fun stopRequest() {
        locationManager.removeUpdates(this)
        listener?.onStopGetMyLocation()
    }

    override fun onLocationChanged(location: Location) {
        listener?.onSuccessGetMyLocation(location)
        locationManager.removeUpdates(this)
        listener?.onStopGetMyLocation()
    }

    private fun start() {
        val enableProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (enableProvider) requestMyLocation()
        else requestLocationSetting()
    }

    private fun requestLocationSetting() {
        AlertDialog.Builder(activity)
            .setMessage("Enable GPS")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, which ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton("No") { dialog, which ->
                dialog.cancel()
            }.create().show()
    }

    private fun dontGrant(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun requestMyLocation() {
        if (dontGrant(Manifest.permission.ACCESS_FINE_LOCATION) && dontGrant(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }
}