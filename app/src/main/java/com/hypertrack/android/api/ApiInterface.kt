package com.hypertrack.android.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @POST("client/devices/{device_id}/start")
    fun makeDriverCheckIn(@Path("device_id") deviceId: String) : Call<Unit>

    @POST("client/devices/{device_id}/stop")
    fun makeDriverCheckOut(@Path("device_id")deviceId : String) : Call<Unit>

    @GET("client/devices/{device_id}/geofences")
    fun getDeliveries(@Path("device_id")deviceId : String) : Call<List<Geofence>>

}

data class Geofence (
    @SerializedName("all_devices") val all_devices : Boolean,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("delete_at") val delete_at : String?,
    @SerializedName("device_id") val device_id : String,
    @SerializedName("device_ids") val device_ids : List<String>,
    @SerializedName("geofence_id") val geofence_id : String,
    @SerializedName("geometry") val geometry : Geometry,
    @SerializedName("metadata") val metadata : Map<String, Any>,
    @SerializedName("radius") val radius : Int,
    @SerializedName("single_use") val single_use : Boolean
)

data class Geometry (
    @SerializedName("coordinates") val coordinates : List<Double>,
    @SerializedName("type") val type : String
)