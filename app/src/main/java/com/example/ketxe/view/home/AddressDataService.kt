package com.example.ketxe.view.home

import com.google.android.gms.maps.model.LatLng

data class Address(val id: UInt, val name: String, val lat: Float, val lon: Float)

fun toJson(address: Address): String {
    return ""
}

interface AddressDataService {
    fun save(name: String, latitude: Double, longitude: Double)
}