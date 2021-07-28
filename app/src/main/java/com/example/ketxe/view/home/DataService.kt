package com.example.ketxe.view.home

import com.example.ketxe.entity.UserIncident
import java.util.*

data class Address(
    val id: String?,
    val description: String,
    val lat: Float,
    val lng: Float
)

data class Stuck(
    val id: String?,
    val addressId: String?,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val updateTime: Date = Date(),
    val severity: StuckSeverity,
    val startTime: Date,
    val fromPoint: String,
    val toPoint: String,
    val isClosedRoad: Boolean,
    val type: StuckType,
    val title: String
)

/**
- 1: Accident
- 2: Congestion
- 3: DisabledVehicle
- 4: MassTransit
- 5: Miscellaneous
- 6: OtherNews
- 7: PlannedEvent
- 8: RoadHazard
- 9: Construction
- 10: Alert
- 11: Weather
 */
enum class StuckType(val code: Int) {
    Accident(1),
    Congestion(2),
    DisabledVehicle(3),
    MassTransit(4),
    Miscellaneous(5),
    OtherNews(6),
    PlannedEvent(7),
    RoadHazard(8),
    Construction(9),
    Alert(10),
    Weather(11)
}

/// Convert code thành kiểu `StuckType`
fun stuckType(code: Int): StuckType {
    return when(code) {
        1 -> StuckType.Accident
        2 -> StuckType.Congestion
        3 -> StuckType.DisabledVehicle
        4 -> StuckType.MassTransit
        5 -> StuckType.Miscellaneous
        6 -> StuckType.OtherNews
        7 -> StuckType.PlannedEvent
        8 -> StuckType.RoadHazard
        9 -> StuckType.Construction
        10 -> StuckType.Alert
        else -> StuckType.Weather
    }
}

enum class StuckSeverity(val code: Int) {
    LowImpact(1),
    Minor(2),
    Moderate(3),
    Serious(4)
}

/// Convert Code thành kiểu `StuckSeverity`
fun stuckSeverity(code: Int): StuckSeverity {
    return when(code) {
        1-> StuckSeverity.LowImpact
        2 -> StuckSeverity.Minor
        3 -> StuckSeverity.Moderate
        else -> StuckSeverity.Serious
    }
}

interface DataService {
    /// Save address vào database.
    fun saveAddress(address: Address, completion: () -> Unit)
    /// Save stuck (điểm kẹt xe) vào database.
    fun save(addressId: String, stucks: List<Stuck>, uincidents: List<UserIncident>, completion: () -> Unit)

    /// Xoá address khỏi database.
    fun deleteAddress(addressId: String, completion: () -> Unit)
    /// Xoá stuck khỏi database.
    fun delete(addressId: String, completion: () -> Unit)

    /// Lấy record Address có id tương ứng.
    fun getAddress(addressId: String): Address?
    /// Lấy tất cả record Address.
    fun getAllAddress(): List<Address>
    /// Lấy tất cả recode stuck liên quan đến Address.
    fun getLastestStuck(addressId: String): List<Stuck>

    fun getLastestIncident(addressId: String): List<UserIncident>
}

