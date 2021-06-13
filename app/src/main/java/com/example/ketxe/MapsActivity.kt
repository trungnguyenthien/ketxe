package com.example.ketxe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ketxe.databinding.ActivityMapsBinding
import com.example.ketxe.view.home.MyLocationRequester
import com.example.ketxe.view.home.MyLocationService
import com.example.ketxe.view.home.MyLocationServiceImpl
import com.example.ketxe.view.home.MyMapFragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MapsActivity : AppCompatActivity() {

//    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var myLocService: MyLocationService = MyLocationServiceImpl(this)


    private val addPinFab: FloatingActionButton by lazy {
        findViewById(R.id.fab)
    }

    private val myLocationFab: FloatingActionButton by lazy {
        findViewById(R.id.fab2)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById(R.id.my_drawer_layout)
    }

    private val actionBarDrawerToggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
    }

    private val navigationViewHolder: NavigationViewHolder by lazy {
        NavigationViewHolder(findViewById(R.id.custom_nav_view))
    }

    private val fragmentManager: FragmentManager by lazy { supportFragmentManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestTrafficPermission(activity = this, code = 4366)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // Show menu button at left navigationBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        addPinFab.setOnClickListener {
//            // Handle click action
//            val lat = mMap.cameraPosition.target.latitude
//            val lon = mMap.cameraPosition.target.longitude
//            Toast.makeText(this, "$lat, $lon", Toast.LENGTH_SHORT).show()
//
//        }

//        myLocationFab.setOnClickListener {
////            clickHandlerOfMyLocationFab()
//            myLocRequester.startRequest(listener = this)
//        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        replaceFragment(ggMyMapFragment)

        myLocService.request(this, onStart = {

        }, onStop = {

        }, onSuccess = {

        })
    }

    val ggMyMapFragment: com.example.ketxe.view.home.MyMapFragment by lazy {
        MyMapFragment()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManger = supportFragmentManager
        val transaction = fragmentManger.beginTransaction()
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        // main_fragment_container is LinearLayout
        transaction.replace(R.id.main_fragment_container, fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }





    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }

    var currentMarker: Marker? = null

//    private fun moveMap(lat: Double, lon: Double, zoom: Double) {
//        currentMarker?.remove()
//        val here = LatLng(lat, lon)
//        currentMarker = mMap.addMarker(MarkerOptions().position(here))
//        mMap.moveCamera(lat, lon, zoom, true)
//    }

}

class NavigationViewHolder(root: View) {
    val addButton: Button by lazy { root.findViewById(R.id.button1) }
    val listView: RecyclerView by lazy { root.findViewById(R.id.listview) }

    class MyViewHolder(root: View) : RecyclerView.ViewHolder(root) {

    }

    class Adapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }


    }


    init {
//        listView.adapter
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



