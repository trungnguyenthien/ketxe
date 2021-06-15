package com.example.ketxe.view.home

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.BindView
import com.example.ketxe.R
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
    private val addMarkerButton: FloatingActionButton by lazy {
        view!!.findViewById(R.id.fab)
    }

    private val myLocationButton: FloatingActionButton by lazy {
        view!!.findViewById(R.id.fab2)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.home_fragment_map, container, false)
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync {
            ggMap = it

            val sydney = LatLng(-34.0, 151.0)
            ggMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            ggMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))

            addMarkerButton.setOnClickListener { onClickAddMarkerButton?.invoke() }
            myLocationButton.setOnClickListener { onClickMyLocationButton?.invoke() }
        }
        return view
    }

    fun centerLocation(): LatLng? = ggMap?.cameraPosition?.target

    var currentMarker: Marker? = null
    private fun addMarker(lat: Double, lon: Double) {
        currentMarker?.remove()
        val here = LatLng(lat, lon)
        currentMarker = ggMap?.addMarker(MarkerOptions().position(here))
    }
}

fun GoogleMap.moveCamera(
    lat: Double,
    lon: Double,
    zoom: Double = 15.0,
    animated: Boolean = true
) {
    val loc = LatLng(lat, lon)
    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(loc, zoom.toFloat())
    if (animated) {
        this.animateCamera(cameraUpdate)
    } else {
        this.moveCamera(cameraUpdate)
    }
}