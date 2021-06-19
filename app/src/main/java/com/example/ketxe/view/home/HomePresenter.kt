package com.example.ketxe.view.home

import android.location.Location
import com.example.ketxe.MapsActivity
import com.google.android.gms.maps.model.LatLng

interface ActivityPresenter {
    fun onStart()
    fun onResume(time: Int)
    fun onPause(time: Int)
    fun onNoLongerVisible()
    fun onDestroyBySystem()
}

interface HomePresenter {
    fun onTapMyLocation()
    fun onTapAddMarker(mapLocation: LatLng)
    fun onSetBackgroundAlarm()
    fun onTapClickAddAddressButton()
    fun onSubmitAddress(addressName: String, location: LatLng)
}

class HomePresenterImpl(val mapsActivity: MapsActivity) : HomePresenter {
    private var myLocService: MyLocationService = MyLocationRequester()
    private var addressService: AddressDataService = DBService()

    override fun onTapMyLocation() {
        myLocService.request(mapsActivity,
            onStart = {
                mapsActivity.showLoadingIndicator(message = "Chờ chút nha, mình đang dò tìm location của bạn...")
            }, onStop = {
                mapsActivity.hideLoadingIndicator()
            }, onSuccess = {
                mapsActivity.moveMapCamera(LatLng(it.latitude, it.longitude))
            })
    }

    override fun onTapAddMarker(mapLocation: LatLng) {
        mapsActivity.addMarker(LatLng(mapLocation.latitude, mapLocation.longitude))
    }

    override fun onSetBackgroundAlarm() {

    }

    override fun onTapClickAddAddressButton() {
        mapsActivity.showInputAddressName()
    }

    override fun onSubmitAddress(addressName: String, location: LatLng) {
//        TODO("Not yet implemented")
        addressService.save(
            name = addressName,
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

}