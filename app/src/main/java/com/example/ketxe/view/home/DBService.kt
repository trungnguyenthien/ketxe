package com.example.ketxe.view.home

import android.app.Application
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class DBService: AddressDataService {
    override fun save(name: String, latitude: Double, longitude: Double) {
        val address = DbAddress().apply {
            this.id = genId()
            this.name = name
            this.latitude = latitude
            this.longitude = longitude
        }

        realm?.executeTransaction {
            it.insert(address)
        }
    }
}

open class DbAddress() : RealmObject() {
    @PrimaryKey var id: String = ""
    var name: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}

open class DbStuck() : RealmObject() {
    @PrimaryKey var id: String = ""
    var addressId: String = ""
    var name: String = ""
    var lattitude: Double = 0.0
    var longitude: Double = 0.0
    var time: Long = 0
}

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