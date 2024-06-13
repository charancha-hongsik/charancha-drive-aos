package com.charancha.drive.retrofit

import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServiceInterface {
    @POST("api/v1/driving")
    fun postDrivingInfo(@Body body: RequestBody): Call<ResponseBody>


}