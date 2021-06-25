package com.example.ketxe

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.ketxe.databinding.ActivityMapsBinding
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import io.realm.Realm


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

    private val navigationView: NavigationView by lazy {
        findViewById<NavigationView>(R.id.custom_nav_view)
    }

    private val addressList: AddressList by lazy {
        navigationView.findViewById<AddressList>(R.id.listview)
    }

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

        hideLoadingIndicator()
    }

    var onResumeCount = 0
    override fun onResume() {
        super.onResume()
        presenter.onResume(onResumeCount++)
    }

    override fun activity(): Activity = this

    override fun addMarker(latlon: LatLng) {
        ggMyMapFragment.addMarker(lat = latlon.latitude, lon = latlon.longitude)
    }

    override fun moveMapCamera(latlon: LatLng) {
        ggMyMapFragment.ggMap?.moveCamera(lat = latlon.latitude, lon = latlon.longitude, zoom = 17.0, animated = true)
    }

    override fun updateAddressList(list: List<Address>) {
        addressList.reloadData(list)
    }

    private val ggMyMapFragment: com.example.ketxe.view.home.MyMapFragment by lazy {
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

class NavigationViewHolder(root: View) {
    val addButton: Button by lazy { root.findViewById<Button>(R.id.button1) }
    val listView: RecyclerView by lazy { root.findViewById<RecyclerView>(R.id.listview) }

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

