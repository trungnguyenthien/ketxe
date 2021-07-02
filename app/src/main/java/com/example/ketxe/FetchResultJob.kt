package com.example.ketxe

import com.example.ketxe.entity.Resources
import com.example.ketxe.view.home.*
import com.google.android.gms.maps.model.LatLng
import io.realm.Realm
import java.util.*

fun connectDB(): RealmDBService? {
    Realm.getDefaultInstance()?.run { return RealmDBService(this) }
    return null
}

class FetchResultJob {
    fun run() {
        connectDB()?.let { db ->
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

        }})
    }

    private fun save(address: Address, resources: List<Resources>) {
        connectDB()?.let { db ->
            val addressId = address.id ?: ""
            val newStucks = resources.map { Stuck(
                id = null,
                addressId = addressId,
                description = it.description,
                latitude = it.toPoint.coordinates[0].toFloat(),
                longitude = it.toPoint.coordinates[1].toFloat(),
                updateTime = Date(),
                severity = stuckSeverity(it.severity)
            )}
            db.saveStuck(addressId = addressId, stucks = newStucks, completion = {
                notifyStuck(address = address, stucks = newStucks)
            })
        }
    }

    private fun notifyStuck(address: Address, stucks: List<Stuck>) {
        val numberSerious = stucks.filter { it.severity == StuckSeverity.Serious }.size
        val numberModerate = stucks.filter { it.severity == StuckSeverity.Moderate }.size
    }
}