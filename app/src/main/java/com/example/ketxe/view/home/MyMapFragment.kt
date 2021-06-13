package com.example.ketxe.view.home

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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyMapFragment : Fragment() {
    var  ggMap: GoogleMap? = null
    @BindView(R.id.fab) lateinit var addMarkerButton: FloatingActionButton
    @BindView(R.id.fab2) lateinit var myLocationButton: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.home_fragment_map, container, false)
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync {
            ggMap = it

            val sydney = LatLng(-34.0, 151.0)
            ggMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            ggMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
        return view
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