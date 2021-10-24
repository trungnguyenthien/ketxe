package com.example.ketxe.view.home

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ketxe.R
import com.example.ketxe.entity.UserIncident
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
    var onClickReportIncidentButton: (() -> Unit)? = null

    private val addMarkerButton: FloatingActionButton get() = view!!.findViewById(R.id.add_marker)
    private val myLocationButton: FloatingActionButton get() = view!!.findViewById(R.id.my_location_button)
    private val addAddressButton: FloatingActionButton get() = view!!.findViewById(R.id.add_address_button)
    private val reportIncidentButton: FloatingActionButton get() = view!!.findViewById(R.id.report_incident_button)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.home_fragment_map, container, false)
        
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if(ggMap == null) supportMapFragment.getMapAsync {
            ggMap = it

            val sydney = LatLng(initLat, initLng)
            addMarker(lat = sydney.latitude, lng = sydney.longitude)
            ggMap?.moveCamera(initLat, initLng, 13.0, true)
            ggMap?.setMapStyle(MapStyleOptions("[\n" +
                    "  {\n" +
                    "    \"featureType\": \"road.arterial\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      { \"color\": \"#CCFFFF\" }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]"))

        }
        return view
    }

    override fun onResume() {
        super.onResume()

        addMarkerButton.setOnClickListener { onClickAddMarkerButton?.invoke() }
        myLocationButton.setOnClickListener { onClickMyLocationButton?.invoke() }
        addAddressButton.setOnClickListener { onClickAddAddressButton?.invoke() }
        reportIncidentButton.setOnClickListener { onClickReportIncidentButton?.invoke() }
    }

    fun centerLocation(): LatLng? = ggMap?.cameraPosition?.target

    private var currentMarker: Marker? = null
    private var currentMarkerLatLng: LatLng? = null
    fun addMarker(lat: Double, lng: Double) {
        currentMarker?.remove()
        currentMarkerLatLng = LatLng(lat, lng)

        currentMarker = ggMap?.addMarker(MarkerOptions().position(currentMarkerLatLng))
    }

    fun currentMarkerLocation(): LatLng? {
        return currentMarkerLatLng
    }

    var initLat: Double = -34.0
    var initLng: Double = 151.0
    fun setInitalizeMarker(lat: Float, lng: Float): Unit {
        initLat = lat.toDouble()
        initLng = lng.toDouble()
    }

    var listStuckPolyline = ArrayList<Polyline>()
    fun clearAllStuckPolygon() {
        listStuckPolyline.forEach { it.remove() }
        listStuckPolyline.clear()
    }

    fun addSeriousLines(stucks: List<Stuck>) {
        addLine(stucks, LineType.SERIOUS)
    }

    fun addNoSeriousLines(stucks: List<Stuck>) {
        addLine(stucks, LineType.NO_SERIOUS)
    }

    private fun addLine(stucks: List<Stuck>, lineType: LineType) {
        waitMapAvailable { ggMap ->
            activity?.runOnUiThread {
                val options = stucks.map { makePolygonOption(lineType, it)}
                val polylines = options.mapNotNull { ggMap.addPolyline(it) }
                listStuckPolyline.addAll(polylines)
            }
        }
    }

    private enum class LineType(val resColorId: Int) {
        SERIOUS(R.color.seriousLine),
        NO_SERIOUS(R.color.noSeriousLine),
        CLOSED_ROAD(R.color.closeRoadLine)
    }

    private fun makePolygonOption(lineType: LineType, stuck: Stuck): PolylineOptions {
        val color =  context?.getColor(lineType.resColorId) ?: 0x00000000
        return PolylineOptions()
            .clickable(true)
            .color(color)
            .width(13f)
            .add(stuck.fromPoint.toLatLng())
            .add(stuck.toPoint.toLatLng())
    }

    fun addClosedRoadLines(stucks: List<Stuck>) {
        addLine(stucks, LineType.CLOSED_ROAD)
    }

    fun clearUIncidents() {
        listStuckMarker.forEach { it.remove() }
        listStuckMarker.clear()
    }

    private fun waitMapAvailable(block: (GoogleMap) -> Unit) {
        Thread {
            while (ggMap == null) { Thread.sleep(50) }
            block.invoke(ggMap!!)
        }.start()
    }

    private var listStuckMarker = ArrayList<Marker>()
    fun renderUIncidents(list: List<UserIncident>) {
        waitMapAvailable { ggMap ->
            activity?.runOnUiThread {
                val options = list.map { makeIncidentMarker(it) }
                val markers = options.map { ggMap.addMarker(it) }.filterNotNull()
                listStuckMarker.addAll(markers)
            }
        }
    }

    private fun makeIncidentMarker(incident: UserIncident): MarkerOptions {
        val position = LatLng(incident.lat.toDouble(), incident.lng.toDouble())
        var resource =  R.drawable.user_incident1

        val size = 65
        val bitmapdraw = resources.getDrawable(resource) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, size, size, false)

        return MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
    }
}

fun String.toLatLng(): LatLng {
    val parts = this.split(",")
    val lat = parts[0].toDouble()
    val lng = parts[1].toDouble()
    return LatLng(lat, lng)
}