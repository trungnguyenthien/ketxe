package com.example.ketxe

import android.location.Location
import com.example.ketxe.entity.Resources
import com.example.ketxe.view.home.Address
import com.example.ketxe.view.home.RealmDBService
import com.example.ketxe.view.home.TrafficBingService
import com.example.ketxe.view.home.TrafficBingServiceImpl
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm

class FetchResultJob {
    fun run() {
        Realm.getDefaultInstance()?.let { realm ->
            val db = RealmDBService(realm)
            fetchAllAddress(db)
            db.printPreviousLog()
            db.saveLog("---")
        }
    }

    private fun fetchAllAddress(db: RealmDBService) {
        db.getAllAddress { list ->
            log("will fetchList size = ${list.size}")
            list.forEach { address ->
                log("will fetch address = ${address.description}")
            processForEach(address, completion = {
                log("Fetch address = ${address.description}")
            })
        }}
    }
    private val api = TrafficBingServiceImpl()
    private fun processForEach(address: Address, completion: (List<Resources>) -> Unit) {

        val ll = LatLng(address.lat.toDouble(), address.lon.toDouble())
        api.request(ll, radius = 5.0, completion = { resources -> resources.forEach { resource ->
            log("-- resource = ${resource.description}")
            log(resource.description)
        }})
    }
}