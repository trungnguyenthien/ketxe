package com.example.ketxe.view.home

import android.app.Activity
import com.example.ketxe.KeyValueStorage
import com.google.android.gms.maps.model.LatLng

interface ActivityPresenter {
    fun onResume(time: Int)
}

interface HomePresenter: ActivityPresenter {
    /// Sự kiện khi user tap vào button Dò Tìm Location
    fun onTapMyLocationButton()
    /// Sự kiện khi user tap vào button Add Marker (gắn marker đỏ vào map)
    fun onTapAddMarkerButton(mapLocation: LatLng)
    /// Sự kiện khi user tap vào button Add Address (thêm toạ độ của marker vào danh sách)
    fun onTapAddAddressButton()
    /// Sự kiện khi user chọn "Đồng Ý", sau khi nhập tên
    fun onSubmitAddress(addressName: String, location: LatLng)
    /// Sự kiện khi user tap button Delete trong AddressList
    fun onTapDeleteButtonOnAddressList(address: Address)
    /// Sự kiện khi ứng dụng được open khi user chọn notification
    fun onOpenFromNotification(addressId: String)
    /// Sự kiện khi user tap vào item (address) trong AddressList
    fun onTapItemOnAddressList(addressId: String)
    /// Sự kiện khi user mở AddressList
    fun onOpenAddressList()
    /// Sự kiện khi user tap vào button "Khởi động service"
    fun onTapStartServiceButton()
    /// Sự kiện khi user tap vào button "Tắt service"
    fun onTapStopServiceButton()
}

interface HomeView {
    /// Cung cấp đối tượng activity cho presenter
    fun activity(): Activity
    /// Thêm marker vào Map
    fun addMarkerOnMap(latLng: LatLng)
    /// Hiển thị dialog để user nhập Address name.
    fun showInputDialogAddressName()
    /// Hiển thị loading indicator
    fun showLoadingIndicator(message: String)
    /// Tắt loading indicator.
    fun hideLoadingIndicator()
    /// Di chuyển camera (góc nhìn) trên map đến location khác.
    fun moveMapCamera(latlng: LatLng)
    /// Cập nhật lại AddressList.
    fun updateAddressList(list: List<HomeAddressRow>)
    /// Xoá các đoạn đường bị kẹt xe hiện tại trên map.
    fun clearAllStuckLines()
    /// Vẽ các đoạn đường bị kẹt NGHIÊM TRỌNG.
    fun renderSeriousStuckLines(seriousStucks: List<Stuck>)
    /// Vẽ các đoạn đường bị kẹt KHÔNG NGHIÊM TRỌNG.
    fun renderNoSeriousStuckLines(noSeriousStucks: List<Stuck>)
    /// Vẽ các đoạn đường bị CHẶN
    fun renderClosedRoadLines(closeRoad: List<Stuck>)
    /// Đóng AddressList.
    fun closeAddressList()
    /// Khởi động service
    fun startService()
    /// Tắt service
    fun stopService()
    /// Cập nhật trạng thái hiển thị Start/Stop button
    fun updateStartStopServiceButton(isStart: Boolean)
}

data class HomeAddressRow(val address: Address, val result: AnalyseResult)

class HomePresenterImpl(private val view: HomeView) : HomePresenter {
    private var myLocService: MyLocationService = MyLocationRequester()
    private var dbService: DataService = RealmDBService()

    override fun onTapMyLocationButton() {
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

    override fun onTapAddMarkerButton(mapLocation: LatLng) {
        view.addMarkerOnMap(LatLng(mapLocation.latitude, mapLocation.longitude))
    }

    override fun onTapAddAddressButton() {
        view.showInputDialogAddressName()
    }

    override fun onSubmitAddress(addressName: String, location: LatLng) {
        val newAddress = Address(
            id = null,
            description = addressName,
            lat = location.latitude.toFloat(),
            lng = location.longitude.toFloat()
        )

        dbService.saveAddress(newAddress, completion = {
            reloadAddressList()
        })
    }

    override fun onTapDeleteButtonOnAddressList(address: Address) {
        address.id?.let { id ->
            dbService.deleteAddress(addressId = id, completion = {
                reloadAddressList()
            })
        }
    }

    override fun onOpenFromNotification(addressId: String) {
        val stucks = dbService.getLastestStuck(addressId)
        val analyseResult = analyse(stucks)
        view.clearAllStuckLines()
        view.renderClosedRoadLines(analyseResult.closesRoads)
        view.renderSeriousStuckLines(analyseResult.serious)
        view.renderNoSeriousStuckLines(analyseResult.noSerious)
    }

    override fun onTapItemOnAddressList(addressId: String) {
        dbService.getAddress(addressId)?.let { address ->
            val stucks = dbService.getLastestStuck(addressId)
            val analyseResult = analyse(stucks)

            val location = LatLng(address.lat.toDouble(), address.lng.toDouble())

            view.clearAllStuckLines()
            view.addMarkerOnMap(location)
            view.moveMapCamera(location)
            view.renderClosedRoadLines(analyseResult.closesRoads)
            view.renderSeriousStuckLines(analyseResult.serious)
            view.renderNoSeriousStuckLines(analyseResult.noSerious)
            view.closeAddressList()
        }
    }

    override fun onOpenAddressList() {
        reloadAddressList()
        val isStop = KeyValueStorage(view.activity()).isBackgroundServiceRunning
        view.updateStartStopServiceButton(!isStop)
    }
    override fun onTapStartServiceButton() {
        view.startService()
        view.closeAddressList()
    }

    override fun onTapStopServiceButton() {
        view.stopService()
        view.closeAddressList()
    }

    override fun onResume(time: Int) = reloadAddressList()

    private fun loadAddressRow(completion: (List<HomeAddressRow>) -> Unit) {
        val outputRows = ArrayList<HomeAddressRow>()

        val list = dbService.getAllAddress()
        list.forEach { address ->
            val addressId = address.id ?: ""
            val stucks = dbService.getLastestStuck(addressId)
            val row = HomeAddressRow(address, analyse(stucks))
            outputRows.add(row)
        }
        completion.invoke(outputRows)
    }

    private fun reloadAddressList() {
        loadAddressRow {
            view.updateAddressList(it)
        }
    }
}

data class AnalyseResult(
    var closesRoads: List<Stuck>,   /// Các đoạn đường bị chặn (bởi cơ quan chức năng)
    var serious: List<Stuck>,       /// Các đoạn đường bị kẹt nghiêm trọng.
    var noSerious: List<Stuck>,     /// Các đoạn đường bị kẹt KHÔNG nghiêm trọng.
    var closesRoadsCount: Int,      /// Số đoạn đường bị chặn.
    var seriousCount: Int,          /// Số đoạn đường bị kẹt Nghiêm trọng.
    var noSeriousCount: Int         /// Số đoạn đường bị kẹt KHÔNG Nghiêm trọng.
)

/// Phân loại các điểm kẹt xe.
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