package com.example.ketxe

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.example.ketxe.databinding.ActivityMapsBinding
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


class MapsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapsBinding

    private val presenter: HomePresenter = HomePresenterImpl(this)

    private val drawerLayout: DrawerLayout by lazy {
        findViewById(R.id.my_drawer_layout)
    }

    private val navigationViewHolder: NavigationViewHolder by lazy {
        NavigationViewHolder(findViewById(R.id.custom_nav_view))
    }
    private val actionBarDrawerToggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
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
    }

    fun addMarker(latlon: LatLng) {

    }

    fun moveMapCamera(latlon: LatLng) {
        ggMyMapFragment.ggMap?.moveCamera(lat = latlon.latitude, lon = latlon.longitude, zoom = 17.0, animated = true)
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

    fun showLoadingIndicator() {
//        TODO("Not yet implemented")
    }

    fun hideLoadingIndicator() {
//        TODO("Not yet implemented")
    }
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

