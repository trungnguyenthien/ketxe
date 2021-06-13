package com.example.ketxe.view.home

import android.location.Location
import com.example.ketxe.entity.IncidentsResponse
import com.example.ketxe.entity.Resources
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface TrafficBingService {
    fun request(location: Location, radius: Double, completion: (List<Resources>) -> UInt)
}

class TrafficBingServiceImpl: TrafficBingService {
    override fun request(location: Location, radius: Double, completion: (List<Resources>) -> UInt) {
        val area = makeBoundingBox(location.latitude, location.longitude, 5.0)
        val call = virtualearthAPI.incident(area.toString(), "3", bingKey)
        call.enqueue(object: Callback<IncidentsResponse> {
            override fun onResponse(call: Call<IncidentsResponse>, response: Response<IncidentsResponse>) {
                if(!response.isSuccessful) {
                    handle(errorCode = response.code())
                    return
                }

                response.body()?.let {
                    completion(it.resourceSets[0].resources)
                }
            }

            fun handle(errorCode: Int) {
                completion(ArrayList<Resources>())
            }

            override fun onFailure(call: Call<IncidentsResponse>, t: Throwable) {
                handle(errorCode = -999)
            }
        })
    }

}

fun makeBoundingBox(lat: Double, lon: Double, radInKm: Double): BoundingBox {
    val latitude: Double = lat
    val longitude: Double = lon

    val radInMetter = radInKm * 1000
    val longitudeD =
        Math.asin(radInMetter / (6378000 * Math.cos(Math.PI * latitude / 180))) * 180 / Math.PI
    val latitudeD = Math.asin(radInMetter.toDouble() / 6378000.toDouble()) * 180 / Math.PI

    val northLat = latitude + latitudeD // NorthLat
    val southLat = latitude - latitudeD // southLat
    val eastLong = longitude + longitudeD // EastLong
    val westLong = longitude - longitudeD // westLong
    return BoundingBox(northLat, eastLong, southLat, westLong)
}


data class BoundingBox(
    val northLat: Double,
    val eastLong: Double,
    val southLat: Double,
    val westLong: Double) {

    override fun toString(): String = "$southLat $westLong $northLat $eastLong"
}

object RetrofitClient {
    private var retrofit: Retrofit? = null
    fun getClient(baseUrl: String?): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}

val virtualearthRetrofit = Retrofit.Builder()
    .baseUrl("http://dev.virtualearth.net")
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

val virtualearthAPI = virtualearthRetrofit.create(API::class.java)