package com.example.ketxe.view.home

import android.app.Activity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

interface ActivityPresenter {
    fun onResume(time: Int)
}

interface HomePresenter: ActivityPresenter {
    fun onTapMyLocation()
    fun onTapAddMarker(mapLocation: LatLng)
    fun onTapClickAddAddressButton()
    fun onSubmitAddress(addressName: String, location: LatLng)
    fun onDelete(address: Address)
    fun onOpenFromNotification(addressId: String)
    fun onTapAddressOnMenu(addressId: String)
}

interface HomeView {
    fun activity(): Activity
    fun addMarker(latLng: LatLng)
    fun showInputAddressName()
    fun showLoadingIndicator(message: String)
    fun hideLoadingIndicator()
    fun moveMapCamera(latlng: LatLng)
    fun updateAddressList(list: List<HomeAddressRow>)
    fun clearAllStuckMarkers()
    fun renderSeriousStuckLines(seriousStucks: List<Stuck>)
    fun renderNoSeriousStuckLines(noSeriousStucks: List<Stuck>)
    fun closeAddressList()
    fun renderClosedRoadLines(closeRoad: List<Stuck>)
}

data class HomeAddressRow(val address: Address, val result: AnalyseResult)

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

    override fun onTapClickAddAddressButton() {
        view.showInputAddressName()
    }

    override fun onSubmitAddress(addressName: String, location: LatLng) {
        dbService.saveAddress(Address(
            id = null,
            description = addressName,
            lat = location.latitude.toFloat(),
            lng = location.longitude.toFloat()
        ) ,completion = {
            onSaveAddressCompletion()
        })
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

    override fun onOpenFromNotification(addressId: String) {
        val stucks = dbService.getLastestStuck(addressId)
        val analyseResult = analyse(stucks)
        view.clearAllStuckMarkers()
        view.renderClosedRoadLines(analyseResult.closesRoads)
        view.renderSeriousStuckLines(analyseResult.serious)
        view.renderNoSeriousStuckLines(analyseResult.noSerious)
    }

    override fun onTapAddressOnMenu(addressId: String) {
        dbService.getAddress(addressId)?.let { address ->
            val stucks = dbService.getLastestStuck(addressId)
            val analyseResult = analyse(stucks)

            val location = LatLng(address.lat.toDouble(), address.lng.toDouble())

            view.clearAllStuckMarkers()
            view.addMarker(location)
            view.moveMapCamera(location)
            view.renderClosedRoadLines(analyseResult.closesRoads)
            view.renderSeriousStuckLines(analyseResult.serious)
            view.renderNoSeriousStuckLines(analyseResult.noSerious)
            view.closeAddressList()
        }
    }

    override fun onResume(time: Int) {
        loadAddressRow {
            view.updateAddressList(it)
        }
    }

    private fun loadAddressRow(completion: (List<HomeAddressRow>) -> Unit) {
        val rows = ArrayList<HomeAddressRow>()

        val list = dbService.getAllAddress()
        list.forEach { address ->
            val addressId = address.id ?: ""
            val stucks = dbService.getLastestStuck(addressId)
            val row = HomeAddressRow(address, analyse(stucks))
            rows.add(row)
        }
        completion.invoke(rows)
    }

    private fun onSaveAddressCompletion() = runBlocking {
        delay(100)
        loadAddressRow {
            view.updateAddressList(it)
        }
    }
}

data class AnalyseResult(
    var closesRoads: List<Stuck>,
    var serious: List<Stuck>,
    var noSerious: List<Stuck>,
    var closesRoadsCount: Int,
    var seriousCount: Int,
    var noSeriousCount: Int
)

fun analyse(stucks: List<Stuck>): AnalyseResult {
    val distinctStucks = stucks.distinctBy { it.title }

    val closeRoad =  distinctStucks.filter { it.isClosedRoad }
    val seriousStucks = distinctStucks.filter { it.severity == StuckSeverity.Serious && !it.isClosedRoad }
    val noSeriousStucks = distinctStucks.filter { it.severity != StuckSeverity.Serious && !it.isClosedRoad }

    val closesRoadsCount = closeRoad.map { it.title }.distinct().size
    val seriousCount = seriousStucks.map { it.title }.distinct().size
    val noSeriousCount = noSeriousStucks.map { it.title }.distinct().size

    return AnalyseResult(
        closeRoad,
        seriousStucks,
        noSeriousStucks,
        closesRoadsCount,
        seriousCount,
        noSeriousCount
    )
}