package com.example.ketxe.entity
import com.google.gson.annotations.SerializedName

data class UserReportResponse(
    @SerializedName("status") val status : Int,
    @SerializedName("message") val message : String
)

data class UserIncident(
    @SerializedName("lat") val lat : Double,
    @SerializedName("lng") val lng : Double,
    @SerializedName("title") val title : String
)