package com.example.ketxe.view.home

import com.example.ketxe.entity.IncidentsResponse
import com.example.ketxe.entity.Resources
import com.example.ketxe.log
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.math.asin
import kotlin.math.cos


interface TrafficBingService {
    fun request(location: LatLng, radius: Double, completion: (List<Resources>) -> Unit)
}

class TrafficBingServiceImpl: TrafficBingService {
    override fun request(location: LatLng, radius: Double, completion: (List<Resources>) -> Unit) {
        val area = makeBoundingBox(location.latitude, location.longitude, 5.0)
        val call = virtualearthAPI.incident(area.toString(), "3,4", bingKey)
        call.enqueue(object: Callback<IncidentsResponse> {
            override fun onResponse(call: Call<IncidentsResponse>, response: Response<IncidentsResponse>) {
                log("Response = ${response.raw()}")
                log("So diem ket xe = ${response.body()?.resourceSets?.first()?.resources?.size}")
                if(!response.isSuccessful) {
                    handle(errorCode = response.code())
                    return
                }

                response.body()?.resourceSets?.first()?.let {
                    completion(it.resources)
                }
            }

            fun handle(errorCode: Int) {
                completion(emptyList())
            }

            override fun onFailure(call: Call<IncidentsResponse>, t: Throwable) {
                log("onFailure(${t.localizedMessage})")
                handle(errorCode = -999)
            }
        })
    }

}

fun makeBoundingBox(lat: Double, lon: Double, radInKm: Double): BoundingBox {
    val latitude: Double = lat
    val longitude: Double = lon

    val radInMeter = radInKm * 1000
    val longitudeD =
        asin(radInMeter / (6378000 * cos(Math.PI * latitude / 180))) * 180 / Math.PI
    val latitudeD = asin(radInMeter / 6378000.toDouble()) * 180 / Math.PI

    val maxLat = latitude + latitudeD
    val minLat = latitude - latitudeD
    val maxLng = longitude + longitudeD
    val minLng = longitude - longitudeD

    return BoundingBox(minLat, minLng, maxLat, maxLng)
}


data class BoundingBox(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double) {

    override fun toString(): String = "$minLat,$minLng,$maxLat,$maxLng"
}

val virtualearthRetrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://dev.virtualearth.net")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val bingKey = "AoAJOXY4xxhJ0CUddHOJfpx9CRnQBWo5OmfS5A2OBexlD4OuRN6QdNeAiSrUB_Jk"

// http://dev.virtualearth.net/REST/v1/Traffic/Incidents/10, 106, 15, 108?severity=3,4&key=AoAJOXY4xxhJ0CUddHOJfpx9CRnQBWo5OmfS5A2OBexlD4OuRN6QdNeAiSrUB_Jk
interface API {
    @GET("REST/v1/Traffic/Incidents/{area}")
    fun incident(
        @Path("area") area: String,
        @Query("severity") severity: String,
        @Query("key") key: String
    ): Call<IncidentsResponse>
}

val virtualearthAPI: API = virtualearthRetrofit.create(API::class.java)