package com.charancha.drive.retrofit

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface ApiServiceInterface {
    @GET("sections")
    fun sections(): Call<JsonObject>


}