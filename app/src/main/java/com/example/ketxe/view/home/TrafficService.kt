package com.example.ketxe.view.home

import com.example.ketxe.entity.IncidentsResponse
import com.example.ketxe.entity.Resources
import com.example.ketxe.entity.UserIncident
import com.example.ketxe.entity.UserReportResponse
import com.example.ketxe.log
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlin.math.asin
import kotlin.math.cos


interface TrafficService {
    fun report(location: LatLng, completion: () -> Unit)
    fun request(location: LatLng, radius: Double, completion: (List<Resources>, List<UserIncident>) -> Unit)
}

class TrafficBingService: TrafficService {
    override fun report(location: LatLng, completion: () -> Unit) {
        val call = kxAPI.add(lat = location.latitude, lng = location.longitude, title = "")
        call.enqueue(object: Callback<UserReportResponse> {
            override fun onResponse(
                call: Call<UserReportResponse>,
                response: Response<UserReportResponse>
            ) {
                completion.invoke()
            }

            override fun onFailure(call: Call<UserReportResponse>, t: Throwable) {
                completion.invoke()
            }
        })
    }

    override fun request(location: LatLng, radius: Double, completion: (List<Resources>, List<UserIncident>) -> Unit) {
        val area = makeBoundingBox(location.latitude, location.longitude, 5.0)
        var output1: List<Resources>? = null
        var output2: List<UserIncident>? = null

        fun complete() {
            if(output1 == null || output2 == null) { return }
            completion.invoke(output1!!, output2!!)
        }
        val veCall = virtualEarthAPI.incident(area.toString(), "3,4", bingKey)
        veCall.enqueue(object: Callback<IncidentsResponse> {
            override fun onResponse(call: Call<IncidentsResponse>, response: Response<IncidentsResponse>) {
                if(!response.isSuccessful) {
                    handle(errorCode = response.code())
                    return
                }

                response.body()?.resourceSets?.first()?.let {
                    output1 = it.resources
                    complete()
                }
            }

            fun handle(errorCode: Int) {
                output1 = emptyList()
                complete()
            }

            override fun onFailure(call: Call<IncidentsResponse>, t: Throwable) {
                log("onFailure(${t.localizedMessage})")
                handle(errorCode = -999)
            }
        })

        var kxCall = kxAPI.get(area = area.toString().replace(" ", ","))
        kxCall.enqueue(object: Callback<UserReportResponse> {
            override fun onResponse(
                call: Call<UserReportResponse>,
                response: Response<UserReportResponse>
            ) {
                if(!response.isSuccessful) {
                    handle(errorCode = response.code())
                    return
                }

                response.body()?.data?.let {
                    output2 = it
                    complete()
                }
            }

            override fun onFailure(call: Call<UserReportResponse>, t: Throwable) {
                handle(errorCode = -999)
            }

            fun handle(errorCode: Int) {
                output2 = emptyList()
                complete()
            }

        })
    }

}

fun makeBoundingBox(lat: Double, lng: Double, radInKm: Double): BoundingBox {
    val radiusEarth = 6371.0

    val latByRad = Math.toRadians(lat);
    val e = radInKm * cos(latByRad)

    val lngDeg = Math.toDegrees(e / radiusEarth)
    val latDeg = Math.toDegrees(radInKm / radiusEarth)

    val minLat = lat - latDeg
    val minLng = lng - lngDeg

    val maxLat = lat + latDeg
    val maxLng = lng + lngDeg

    return BoundingBox(
        minLat, minLng, /** Top-Left Point **/
        maxLat, maxLng  /** Bottom-Right Point **/
    )
}


data class BoundingBox(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double) {

    override fun toString(): String = "$minLat,$minLng,$maxLat,$maxLng"
}

val virtualEarthRetrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://dev.virtualearth.net")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val bingKey = "AoAJOXY4xxhJ0CUddHOJfpx9CRnQBWo5OmfS5A2OBexlD4OuRN6QdNeAiSrUB_Jk"

// http://dev.virtualearth.net/REST/v1/Traffic/Incidents/10, 106, 15, 108?severity=3,4&key=AoAJOXY4xxhJ0CUddHOJfpx9CRnQBWo5OmfS5A2OBexlD4OuRN6QdNeAiSrUB_Jk
interface VirtualEarthAPI {
    @GET("REST/v1/Traffic/Incidents/{area}")
    fun incident(
        @Path("area") area: String,
        @Query("severity") severity: String,
        @Query("key") key: String
    ): Call<IncidentsResponse>
}

val virtualEarthAPI: VirtualEarthAPI = virtualEarthRetrofit.create(VirtualEarthAPI::class.java)

val KetXeAsiaRetrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://ketxe.asia")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface KxAPI {
    @GET("get.php")
    fun get(
        @Query("area") area: String
    ): Call<UserReportResponse>

    @FormUrlEncoded @POST("add.php")
    fun add(
        @Field("lat") lat: Double,
        @Field("lng") lng: Double,
        @Field("title") title: String
    ): Call<UserReportResponse>
}

val kxAPI: KxAPI = KetXeAsiaRetrofit.create(KxAPI::class.java)
