package com.example.ketxe.view.home

import android.app.Application
import android.content.Context
import com.example.ketxe.entity.UserIncident
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where
import java.util.*

open class RealmDBService : DataService {
    override fun saveAddress(address: Address, completion: () -> Unit) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val dto = DbAddress().apply {
                this.id = genId()
                this.description = address.description
                this.latitude = address.lat
                this.longitude = address.lng
                this.startTime = address.startTime
                this.endTime = address.endTime
            }
            it.insert(dto)
        }
        realm.close()
        completion.invoke()
    }

    override fun save(addressId: String, stucks: List<Stuck>, uincidents: List<UserIncident>, completion: () -> Unit) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val newStucks = stucks.map { entity ->
                DbStuck().apply {
                    this.id = genId()
                    this.addressId = addressId
                    this.latitude = entity.latitude
                    this.longitude = entity.longitude
                    this.description = entity.description
                    this.updateTime = entity.updateTime
                    this.severity = entity.severity.code
                    this.startTime = entity.startTime
                    this.fromPoint = entity.fromPoint
                    this.toPoint = entity.toPoint
                    this.isClosedRoad = entity.isClosedRoad
                    this.type = entity.type.code
                    this.title = entity.title
                }
            }
            it.insert(newStucks)
        }

        realm.executeTransaction {
            val newIncidens = uincidents.map { entity ->
                DbIncident().apply {
                    this.id = genId()
                    this.addressId = addressId
                    this.latitude = entity.lat
                    this.longitude = entity.lng
                }
            }
            it.insert(newIncidens)
        }

        realm.close()
        completion.invoke()
    }

    override fun deleteAddress(addressId: String, completion: () -> Unit) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val list = it.where<DbAddress>()
                .equalTo("id", addressId)
                .findAll()
            list.deleteAllFromRealm()
        }
        realm.close()
        completion.invoke()
    }

    override fun delete(addressId: String, completion: () -> Unit) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val list = it.where<DbStuck>()
                .equalTo("addressId", addressId)
                .findAll()
            list.deleteAllFromRealm()
        }

        realm.executeTransaction {
            val list = it.where<DbIncident>()
                .equalTo("addressId", addressId)
                .findAll()
            list.deleteAllFromRealm()
        }

        realm.close()
        completion.invoke()
    }

    override fun getAddress(addressId: String): Address? {
        val realm = Realm.getDefaultInstance()
        realm.run {
            val output = this.where<DbAddress>()
                .equalTo("id", addressId)
                .findFirst()
            realm.close()
            return output?.toEntity()
        }
        realm.close()
        return null
    }

    override fun getAllAddress(): List<Address> {
        val realm = Realm.getDefaultInstance()
        realm.run {
            val addresses = where<DbAddress>()
                .alwaysTrue()
                .findAll()
                .map { it.toEntity() }
            realm?.close()
            return addresses
        }
        realm.close()
        return emptyList()
    }

    override fun getLastestStuck(addressId: String): List<Stuck> {
        val realm = Realm.getDefaultInstance()
        realm?.run {
            val output = where<DbStuck>()
                .equalTo("addressId", addressId)
                .findAll().map { it.toEntity() }
            realm?.close()
            return output
        }
        realm?.close()
        return emptyList()
    }

    override fun getLastestIncident(addressId: String): List<UserIncident> {
        val realm = Realm.getDefaultInstance()
        realm?.run {
            val output = where<DbIncident>()
                .equalTo("addressId", addressId)
                .findAll().map { it.toEntity() }
            realm?.close()
            return output
        }
        realm?.close()
        return emptyList()
    }
}

open class DbAddress : RealmObject() {
    @PrimaryKey var id: String = ""
    var description: String = ""
    var latitude: Float = 0.0f
    var longitude: Float = 0.0f
    var startTime: Int = 0
    var endTime: Int = 0
}

open class DbStuck : RealmObject() {
    @PrimaryKey var id: String = ""
    var addressId: String = ""
    var description: String = ""
    var latitude: Float = 0.0f
    var longitude: Float = 0.0f
    var updateTime: Date = Date()
    var severity = 0
    var startTime = Date()
    var fromPoint = ""
    var toPoint = ""
    var isClosedRoad: Boolean = false
    var type: Int = 0
    var title = ""
}

fun DbStuck.toEntity() = Stuck(
    id,
    addressId,
    description,
    latitude,
    longitude,
    updateTime,
    stuckSeverity(severity),
    startTime,
    fromPoint,
    toPoint,
    isClosedRoad,
    stuckType(type),
    title
)

open class DbIncident : RealmObject() {
    @PrimaryKey var id: String = ""
    var addressId: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}

fun DbIncident.toEntity() = UserIncident(lat = latitude, lng = longitude)

fun DbAddress.toEntity() = Address(id, description, latitude, longitude, startTime, endTime)

fun genId(): String {
    val id = UUID.randomUUID().toString()
    return id
}

var globalRealm: Realm? = null
fun initRealm(context: Context) {
    Realm.init(context)
    val config = RealmConfiguration.Builder()
        .name("KetXe")
        .deleteRealmIfMigrationNeeded()
        .schemaVersion(1)
        .allowQueriesOnUiThread(true)
        .allowWritesOnUiThread(true)
        .build()
    Realm.setDefaultConfiguration(config)
    globalRealm = Realm.getDefaultInstance()
}


open class MyApp: Application() {
    override fun onCreate() {
        initRealm(this)
        super.onCreate()
    }
}