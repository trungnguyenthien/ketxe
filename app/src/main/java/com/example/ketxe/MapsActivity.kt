package com.example.ketxe

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ketxe.databinding.ActivityMapsBinding
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng


class MapsActivity : AppCompatActivity(), HomeView {

    private lateinit var binding: ActivityMapsBinding

    private val presenter: HomePresenter = HomePresenterImpl(this)

    private val drawerLayout: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.my_drawer_layout)
    }

    private val loadingLayout: RelativeLayout by lazy {
        findViewById<RelativeLayout>(R.id.loading_layout)
    }

    private val loadingText: TextView by lazy {
        findViewById<TextView>(R.id.loading_text)
    }

    private val actionBarDrawerToggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
    }

    private val customAdapter: AddressList.Adapter by lazy {
        var adapter = AddressList.Adapter(this)
        addressList.adapter = adapter
        addressList.layoutManager = LinearLayoutManager(this)
        addressList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter.onDeleteItem = { onDelete(it) }
        adapter.onClickAddress = { onClickAddressOnList(it) }
        return@lazy adapter
    }

    private fun onDelete(address: Address) {
        presenter.onDelete(address = address)
    }

    private fun onClickAddressOnList(address: Address) {
        address.id?.let { addressId ->
            presenter.onTapAddressOnMenu(addressId = addressId)
        }
    }

    private fun reloadData(list: List<HomeAddressRow>) {
        customAdapter.update(list)
    }

    private val addressList: AddressList by lazy {
        findViewById<AddressList>(R.id.listview)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestTrafficPermission(activity = this, code = 4366)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // Show menu button at left navigationBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        replaceFragment(ggMyMapFragment)

        ggMyMapFragment.onClickAddMarkerButton = {
            ggMyMapFragment.centerLocation()?.let {
                presenter.onTapAddMarker(it)
            }
        }

        ggMyMapFragment.onClickMyLocationButton = {
            presenter.onTapMyLocation()
        }

        ggMyMapFragment.onClickAddAddressButton = {
            presenter.onTapClickAddAddressButton()
        }

        var addressFromNotif = intent.extras?.get("address") as? String
        addressFromNotif?.let { addressId ->
            val lat = intent.extras?.get("lat") as Float
            val lon = intent.extras?.get("lon") as Float
            ggMyMapFragment.setInitalizeMarker(lat = lat, lon = lon)
            presenter.onOpenFromNotification(addressId = addressId)
        }

        hideLoadingIndicator()
        startServiceIfNeed()
    }

    private fun startServiceIfNeed() {
        if(isGranted(Manifest.permission.FOREGROUND_SERVICE)) {
            MyJobService.startJob(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startServiceIfNeed()
    }

    private var onResumeCount = 0
    override fun onResume() {
        super.onResume()
        presenter.onResume(onResumeCount++)
    }

    override fun activity(): Activity = this

    override fun addMarker(latlon: LatLng) {
        ggMyMapFragment.addMarker(lat = latlon.latitude, lon = latlon.longitude)
    }

    override fun moveMapCamera(latlon: LatLng) {
        ggMyMapFragment.ggMap?.moveCamera(latlon.latitude, latlon.longitude, 13.0, true)
    }

    override fun updateAddressList(list: List<HomeAddressRow>) {
        reloadData(list)
    }

    override fun clearAllStuckMarkers() {
        ggMyMapFragment.clearAllStuckPolygon()
    }

    override fun renderSeriousStuckMarkers(seriousStucks: List<Stuck>) {
        ggMyMapFragment.addSeriousStuckMarkers(stucks = seriousStucks)
    }

    override fun renderNoSeriousStuckMarkers(noSeriousStucks: List<Stuck>) {
        ggMyMapFragment.addNoSeriousStuckMarkers(stucks = noSeriousStucks)
    }

    override fun closeAddressList() {
        drawerLayout.closeDrawers()
    }

    private val ggMyMapFragment: MyMapFragment by lazy {
        MyMapFragment()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManger = supportFragmentManager
        val transaction = fragmentManger.beginTransaction()
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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

    override fun showLoadingIndicator(message: String) {
        loadingText.text = message
        loadingLayout.visibility = View.VISIBLE
    }

    override fun hideLoadingIndicator() {
        loadingText.text = ""
        loadingLayout.visibility = View.INVISIBLE
    }

    override fun showInputAddressName() {
        val taskEditText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Giải pháp tránh Kẹt xe")
            .setMessage("Nhập tên địa điểm này:")
            .setView(taskEditText)
            .setPositiveButton("Hoàn tất") { _, _ ->
                ggMyMapFragment.currentMarkerLocation()?.let {
                    val addressName = taskEditText.text.toString()
                    presenter.onSubmitAddress(addressName = addressName, location = it)
                }
            }
            .setNegativeButton("Từ chối", null)
            .create()
        dialog.show()
    }
}

fun requestTrafficPermission(activity: Activity, code: Int) {
    PermissionRequester(
        activity = activity,
        permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE),
        requestCode = code
    ).requestIfNeed()
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

fun Context.isGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}