package com.example.ketxe.entity
import com.google.gson.annotations.SerializedName

data class UserReportResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : List<UserIncident>?
)

data class UserIncident(
    @SerializedName("lat") val lat : Double,
    @SerializedName("lng") val lng : Double
)