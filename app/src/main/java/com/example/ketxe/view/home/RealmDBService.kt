package com.example.ketxe.view.home

import android.app.Application
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.runBlocking
import java.util.*

open class RealmDBService: DataService {
    override fun saveAddress(address: Address, completion: () -> Unit) {
        realm?.executeTransaction {
            val dto = DbAddress().apply {
                this.id = genId()
                this.description = address.description
                this.latitude = address.lat
                this.longitude = address.lon
            }
            it.insert(dto)
        }
        completion.invoke()
    }

    override fun saveStuck(addressId: String, stucks: List<Stuck>, completion: () -> Unit) {
        realm?.executeTransaction {
            val list = stucks.map { entity ->
                DbStuck().apply {
                    this.id = genId()
                    this.addressId = addressId
                    this.latitude = entity.latitude
                    this.longitude = entity.longitude
                    this.description = entity.description
                    this.updateTime = entity.updateTime
                }
            }
            it.insert(list)
            completion.invoke()
        }
    }

    override fun deleteAddress(addressId: String, completion: () -> Unit) {
        realm?.executeTransaction {
            val list = it.where<DbAddress>()
                .equalTo("id", addressId)
                .findAll()
            list.deleteAllFromRealm()
            completion.invoke()
        }
    }

    override fun deleteStuck(addressId: String, completion: () -> Unit) {
        realm?.executeTransaction {
            val list = it.where<DbStuck>()
                .equalTo("addressId", addressId)
                .findAll()
            list.deleteAllFromRealm()
            completion.invoke()
        }
    }

    override fun getAllAddress(completion: (List<Address>) -> Unit) {
        realm?.executeTransaction { realm ->
            var list = realm.where<DbAddress>()
                .alwaysTrue()
                .findAll()
                .map { it.toEntity() }
            completion.invoke(list)
        }
    }

    override fun getLastestStuck(addressId: String, completion: (List<Stuck>) -> Unit) {
        realm?.executeTransaction { realm ->
            var list = realm.where<DbStuck>()
                .equalTo("addressId", addressId)
                .findAll().map { it.toEntity() }
            completion.invoke(list)
        }
    }
}

open class DbAddress : RealmObject() {
    @PrimaryKey var id: String = ""
    var description: String = ""
    var latitude: Float = 0.0f
    var longitude: Float = 0.0f
}

open class DbStuck : RealmObject() {
    @PrimaryKey var id: String = ""
    var addressId: String = ""
    var description: String = ""
    var latitude: Float = 0.0f
    var longitude: Float = 0.0f
    var updateTime: Date = Date()
}

fun DbStuck.toEntity() = Stuck(id, addressId, description, latitude, longitude, updateTime)
fun DbAddress.toEntity() = Address(id, description, latitude, longitude)

fun genId(): String {
    val id = UUID.randomUUID().toString()
    println("New ID = $id")
    return id
}

var realm: Realm? = null
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
    realm = Realm.getDefaultInstance()
}


open class MyApp: Application() {
    override fun onCreate() {
        initRealm(this)
        super.onCreate()
    }
}