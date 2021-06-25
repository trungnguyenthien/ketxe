package com.example.ketxe.view.home

import java.util.*

data class Address(
    val id: String?,
    val description: String,
    val lat: Float,
    val lon: Float
)

data class Stuck(
    val id: String?,
    val addressId: String?,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val updateTime: Date = Date()
)

interface DataService {
    fun saveAddress(address: Address, completion: () -> Unit)
    fun saveStuck(addressId: String, stucks: List<Stuck>, completion: () -> Unit)

    fun deleteAddress(addressId: String, completion: () -> Unit)
    fun deleteStuck(addressId: String, completion: () -> Unit)

    fun getAllAddress(completion: (List<Address>) -> Unit)
    fun getLastestStuck(addressId: String, completion: (List<Stuck>) -> Unit)
}

