package com.charancha.drive.retrofit

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServiceInterface {
    @POST("driving")
    fun sections(): Call<JsonObject>


}