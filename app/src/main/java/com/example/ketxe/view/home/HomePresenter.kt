package com.example.ketxe.view.home

import android.app.Activity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

interface ActivityPresenter {
    fun onStart()
    fun onResume(time: Int)
    fun onPause(time: Int)
    fun onNoLongerVisible()
    fun onDestroyBySystem()
}

interface HomePresenter: ActivityPresenter {
    fun onTapMyLocation()
    fun onTapAddMarker(mapLocation: LatLng)
    fun onSetBackgroundAlarm()
    fun onTapClickAddAddressButton()
    fun onSubmitAddress(addressName: String, location: LatLng)
    fun onDelete(address: Address)
    fun didOpenFromNotification(addressId: String)
}

interface HomeView {
    fun activity(): Activity
    fun addMarker(latlon: LatLng)
    fun showInputAddressName()
    fun showLoadingIndicator(message: String)
    fun hideLoadingIndicator()
    fun moveMapCamera(latlon: LatLng)
    fun updateAddressList(list: List<HomeAddressRow>)
    fun clearAllStuckMarkers()
    fun renderSeriousStuckMarkers(seriousStucks: List<Stuck>)
    fun renderNoSeriousStuckMarkers(noSeriousStucks: List<Stuck>)
}

data class HomeAddressRow(val address: Address, val serious: Int, val noSerious: Int)

class HomePresenterImpl(private val view: HomeView) : HomePresenter {
    private var myLocService: MyLocationService = MyLocationRequester()
    private var dbService: DataService = RealmDBService()

    override fun onTapMyLocation() {
        myLocService.request(view.activity(),
            onStart = {
                view.showLoadingIndicator(message = "Chờ chút nha, mình đang dò tìm location của bạn...")
            }, onStop = {
                view.hideLoadingIndicator()
            }, onSuccess = {
                view.moveMapCamera(LatLng(it.latitude, it.longitude))
            }
        )
    }

    override fun onTapAddMarker(mapLocation: LatLng) {
        view.addMarker(LatLng(mapLocation.latitude, mapLocation.longitude))
    }

    override fun onSetBackgroundAlarm() {

    }

    override fun onTapClickAddAddressButton() {
        view.showInputAddressName()
    }

    override fun onSubmitAddress(addressName: String, location: LatLng) {
        dbService.saveAddress(Address(
            null,
            addressName,
            location.latitude.toFloat(),
            location.longitude.toFloat()
        ) ,completion = { onSaveAddressCompletion() })
    }

    override fun onDelete(address: Address) {
        address.id?.let { id ->
            dbService.deleteAddress(addressId = id, completion = {
                loadAddressRow {
                    view.updateAddressList(it)
                }
            })
        }
    }

    override fun didOpenFromNotification(addressId: String) {
        dbService.getLastestStuck(addressId = addressId, completion = { stucks ->
            val seriousStucks = stucks.filter { it.severity == StuckSeverity.Serious }
            var noSeriousStucks = stucks.filter { it.severity != StuckSeverity.Serious }
            view.clearAllStuckMarkers()
            view.renderSeriousStuckMarkers(seriousStucks)
            view.renderNoSeriousStuckMarkers(noSeriousStucks)
        })
    }

    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onResume(time: Int) {
//        TODO("Not yet implemented")
        loadAddressRow {
            view.updateAddressList(it)
        }
    }

    private fun loadAddressRow(completion: (List<HomeAddressRow>) -> Unit) {
        var rows = ArrayList<HomeAddressRow>()
        dbService.getAllAddress { list -> list.forEach { address ->
            val addressId = address.id ?: ""
            val isLastAddress = address == list.last()

            dbService.getLastestStuck(addressId = addressId, completion = { stucks ->
                val serious = stucks.filter { it.severity == StuckSeverity.Serious }.size
                val noSerious = stucks.filter { it.severity != StuckSeverity.Serious }.size
                val row = HomeAddressRow(address, serious, noSerious)
                rows.add(row)

                if(isLastAddress) { completion.invoke(rows) }
            })
        }}
    }

    override fun onPause(time: Int) {
//        TODO("Not yet implemented")
    }

    override fun onNoLongerVisible() {
//        TODO("Not yet implemented")
    }

    override fun onDestroyBySystem() {
//        TODO("Not yet implemented")
    }

    private fun onSaveAddressCompletion() = runBlocking {
        delay(100)
        loadAddressRow {
            view.updateAddressList(it)
        }
    }
}