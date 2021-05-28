package com.example.ketxe

import IncidentsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


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

val sharedRetrofit = Retrofit.Builder()
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
    ): IncidentsResponse
}