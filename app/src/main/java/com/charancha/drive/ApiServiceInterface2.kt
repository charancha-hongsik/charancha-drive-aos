package com.charancha.drive

import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServiceInterface2 {
    @POST("hashed-account")
    fun postDrivingInfo(@Body body: RequestBody): Call<ResponseBody>
}