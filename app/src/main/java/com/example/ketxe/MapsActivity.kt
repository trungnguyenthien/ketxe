package com.example.ketxe

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
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
import com.example.ketxe.entity.UserIncident
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapsActivity : AppCompatActivity(), HomeView, DrawerLayout.DrawerListener {

    private lateinit var binding: ActivityMapsBinding

    private val presenter: HomePresenter = HomePresenterImpl(this)

    private val startServiceButton: Button by lazy {
        findViewById<Button>(R.id.btn_start)
    }

    private val stopServiceButton: Button by lazy {
        findViewById<Button>(R.id.btn_stop)
    }

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
        adapter.onClickDebug = { onClickDebugOnList(it) }
        return@lazy adapter
    }

    private fun onDelete(address: Address) {
        presenter.onTapDeleteButtonOnAddressList(address = address)
    }

    private fun onClickAddressOnList(address: Address) {
        address.id?.let { addressId ->
            presenter.onTapItemOnAddressList(addressId = addressId)
        }
    }

    private fun onClickDebugOnList(address: Address) {
        address?.let {
            presenter.onTapDebugOnAddressList(address = it)
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

        drawerLayout.addDrawerListener(this)
        // Show menu button at left navigationBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        replaceFragment(ggMyMapFragment)

        ggMyMapFragment.onClickAddMarkerButton = {
            ggMyMapFragment.centerLocation()?.let {
                presenter.onTapAddMarkerButton(it)
            }
        }

        ggMyMapFragment.onClickMyLocationButton = {
            presenter.onTapMyLocationButton()
        }

        ggMyMapFragment.onClickAddAddressButton = {
            presenter.onTapAddAddressButton()
        }

        ggMyMapFragment.onClickReportIncidentButton = {
            presenter.onTapIncidentReportAtMyLocation()
        }

        startServiceButton.setOnClickListener {
            presenter.onTapStartServiceButton()
        }

        stopServiceButton.setOnClickListener {
            presenter.onTapStopServiceButton()
        }

        startServiceButton.visibility = View.GONE
        stopServiceButton.visibility = View.GONE

        var addressFromNotif = intent.extras?.get("address") as? String
        addressFromNotif?.let { addressId ->
            val lat = intent.extras?.get("lat") as Float
            val lng = intent.extras?.get("lng") as Float
            ggMyMapFragment.setInitalizeMarker(lat = lat, lng = lng)
            presenter.onOpenFromNotification(addressId = addressId)
        }

        hideLoadingIndicator()
    }

    private var onResumeCount = 0
    override fun onResume() {
        super.onResume()
        presenter.onResume(onResumeCount++)
    }

    override fun activity(): Activity = this

    override fun addMarkerOnMap(latLng: LatLng) {
        ggMyMapFragment.addMarker(lat = latLng.latitude, lng = latLng.longitude)
    }

    override fun moveMapCamera(latlng: LatLng) {
        ggMyMapFragment.ggMap?.moveCamera(latlng.latitude, latlng.longitude, 13.0, true)
    }

    override fun updateAddressList(list: List<HomeAddressRow>) {
        reloadData(list)
    }

    override fun clearAllStuckLines() {
        ggMyMapFragment.clearAllStuckPolygon()
    }

    override fun renderSeriousStuckLines(seriousStucks: List<Stuck>) {
        ggMyMapFragment.addSeriousLines(stucks = seriousStucks)
    }

    override fun renderNoSeriousStuckLines(noSeriousStucks: List<Stuck>) {
        ggMyMapFragment.addNoSeriousLines(stucks = noSeriousStucks)
    }

    override fun closeAddressList() {
        drawerLayout.closeDrawers()
    }

    override fun startService() {
        if (isGranted(Manifest.permission.FOREGROUND_SERVICE)) {
            MyForegroundService.start(this)
        }
    }

    override fun stopService() {
        MyForegroundService.stop(this)
    }

    override fun updateStartStopServiceButton(isStart: Boolean) {
        if(isStart) {
            startServiceButton.visibility = View.VISIBLE
            stopServiceButton.visibility = View.GONE
        } else {
            startServiceButton.visibility = View.GONE
            stopServiceButton.visibility = View.VISIBLE
        }
    }

    override fun clearAllUIncidents() {
        ggMyMapFragment.clearUIncidents()
    }

    override fun renderUIncidents(list: List<UserIncident>) {
        ggMyMapFragment.renderUIncidents(list = list)
    }

    override fun clipboard(text: String) {
        val clipboard = /* context. */ getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("log", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun renderClosedRoadLines(closeRoad: List<Stuck>) {
        ggMyMapFragment.addClosedRoadLines(stucks = closeRoad)
    }

    private val ggMyMapFragment: MyMapFragment by lazy {
        MyMapFragment()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManger = supportFragmentManager
        val transaction = fragmentManger.beginTransaction()
        transaction.setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
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

    override fun showInputDialogAddressName() {
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

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

    override fun onDrawerOpened(drawerView: View) {
        presenter.onOpenAddressList()
    }

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerStateChanged(newState: Int) {}
}

fun requestTrafficPermission(activity: Activity, code: Int) {
    PermissionRequester(
        activity = activity,
        permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        ),
        requestCode = code
    ).requestIfNeed()
}

fun GoogleMap.moveCamera(
    lat: Double,
    lng: Double,
    zoom: Double = 15.0,
    animated: Boolean = true
) {
    val loc = LatLng(lat, lng)
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