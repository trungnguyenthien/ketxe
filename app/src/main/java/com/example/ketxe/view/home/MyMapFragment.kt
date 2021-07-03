package com.example.ketxe.view.home

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ketxe.R
import com.example.ketxe.moveCamera
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MyMapFragment : Fragment() {
    var  ggMap: GoogleMap? = null

    var onClickAddMarkerButton: (() -> Unit)? = null
    var onClickMyLocationButton: (() -> Unit)? = null
    var onClickAddAddressButton: (() -> Unit)? = null
    private val addMarkerButton: FloatingActionButton by lazy {
        view!!.findViewById<FloatingActionButton>(R.id.add_marker)
    }

    private val myLocationButton: FloatingActionButton by lazy {
        view!!.findViewById<FloatingActionButton>(R.id.my_location_button)
    }

    private val addAddressButton: FloatingActionButton by lazy {
        view!!.findViewById<FloatingActionButton>(R.id.add_address_button)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.home_fragment_map, container, false)
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync {
            ggMap = it

            val sydney = LatLng(initLat, initLon)
            addMarker(lat = sydney.latitude, lon = sydney.longitude)
            ggMap?.moveCamera(initLat, initLon, 13.0, true)
            addMarkerButton.setOnClickListener { onClickAddMarkerButton?.invoke() }
            myLocationButton.setOnClickListener { onClickMyLocationButton?.invoke() }
            addAddressButton.setOnClickListener { onClickAddAddressButton?.invoke() }
        }
        return view
    }

    fun centerLocation(): LatLng? = ggMap?.cameraPosition?.target

    private var currentMarker: Marker? = null
    private var currentMarkerLatLng: LatLng? = null
    fun addMarker(lat: Double, lon: Double) {
        currentMarker?.remove()
        currentMarkerLatLng = LatLng(lat, lon)

        currentMarker = ggMap?.addMarker(MarkerOptions().position(currentMarkerLatLng))
    }

    fun currentMarkerLocation(): LatLng? {
        return currentMarkerLatLng
    }

    var initLat: Double = -34.0
    var initLon: Double = 151.0
    fun setInitalizeMarker(lat: Float, lon: Float): Unit {
        initLat = lat.toDouble()
        initLon = lon.toDouble()
    }

    var listStuckPolyline = ArrayList<Polyline>()
    fun clearAllStuckPolygon() {
        listStuckPolyline.forEach { it.remove() }
        listStuckPolyline.clear()
    }

    fun addSeriousStuckMarkers(stucks: List<Stuck>) {
        fun addPolyline() {
            activity?.runOnUiThread {
                val options = stucks.map { makePolygonOption(true, it)}
                val polylines = options.map { ggMap?.addPolyline(it) }.filterNotNull()
                listStuckPolyline.addAll(polylines)
            }
        }
        Thread {
            while (ggMap == null) { Thread.sleep(200) }
            addPolyline()
        }.start()
    }

    fun addNoSeriousStuckMarkers(stucks: List<Stuck>) {
        fun addPolyline() {
            activity?.runOnUiThread {
                val options = stucks.map { makePolygonOption(false, it)}
                val polylines = options.map { ggMap?.addPolyline(it) }.filterNotNull()
                listStuckPolyline.addAll(polylines)
            }
        }
        Thread {
            while (ggMap == null) { Thread.sleep(200) }
            addPolyline()
        }.start()
    }

    private fun makePolygonOption(isSerious: Boolean, stuck: Stuck): PolylineOptions {
        val seriousColor =  context?.getColor(R.color.seriousLine) ?: 0x00000000
        val noSeriousColor =  context?.getColor(R.color.noSeriousLine) ?: 0x00000000
        val strokeColor = if(isSerious) seriousColor else noSeriousColor
        return PolylineOptions()
            .clickable(true)
            .color(strokeColor)
            .width(13f)
            .add(stuck.fromPoint.toLatLng())
            .add(stuck.toPoint.toLatLng())
    }
}

fun String.toLatLng(): LatLng {
    val parts = this.split(",")
    val lat = parts[0].toDouble()
    val lng = parts[1].toDouble()
    return LatLng(lat, lng)
}