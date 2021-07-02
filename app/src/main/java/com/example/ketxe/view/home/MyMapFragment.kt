package com.example.ketxe.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ketxe.R
import com.example.ketxe.moveCamera
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
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
}
