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

    @POST("api/v1/auth/test/signup")
    fun postSignUp(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/signin")
    fun postSignIn(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/token/reissue")
    fun postReissue(@Header("refresh_token") token: String): Call<ResponseBody>

    @GET("api/v1/terms/summary")
    fun getTerms(@Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/terms/{id}")
    fun getTermDetails(@Path("id") userKey: String): Call<ResponseBody>

    @POST("api/v1/terms/agree")
    fun postTermsAgree(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/terms/agree/status")
    fun getTermsAgree(@Body body: RequestBody): Call<ResponseBody>

}