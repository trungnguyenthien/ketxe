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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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

    var listStuckMarker = ArrayList<Marker>()
    fun clearAllStuckMarkers() {
        listStuckMarker.forEach { it.remove() }
        listStuckMarker.clear()
    }

    fun addSeriousStuckMarkers(stucks: List<Stuck>) {
        fun addMarker() {
            activity?.runOnUiThread {
                ggMap?.let { ggMap ->
                    val options = stucks.map { makeStuckMarker(true, it) }
                    val markers = options.map { ggMap.addMarker(it) }.filterNotNull()
                    listStuckMarker.addAll(markers)
                }
            }
        }
        Thread {
            while (ggMap == null) { Thread.sleep(200) }
            addMarker()
        }.start()
    }

    fun addNoSeriousStuckMarkers(stucks: List<Stuck>) {
        fun addMarker() {
            activity?.runOnUiThread {
                ggMap?.let { ggMap ->
                    val options = stucks.map { makeStuckMarker(false, it) }
                    val markers = options.map { ggMap.addMarker(it) }.filterNotNull()
                    listStuckMarker.addAll(markers)
                }
            }
        }
        Thread {
            while (ggMap == null) { Thread.sleep(200) }
            addMarker()
        }.start()
    }

    private fun makeStuckMarker(isSerious: Boolean, stuck: Stuck): MarkerOptions {
        val position = LatLng(stuck.latitude.toDouble(), stuck.longitude.toDouble())
        var resource = if(isSerious) R.drawable.s_ico else R.drawable.m_ico

        val size = 65
        val bitmapdraw = resources.getDrawable(resource) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, size, size, false)

        return MarkerOptions()
            .position(position)
            .title(stuck.description)
            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
    }
}
