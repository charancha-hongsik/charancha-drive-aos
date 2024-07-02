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

    @POST("api/v1/auth/signup")
    fun postSignUp(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/signin")
    fun postSignIn(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/token/reissue")
    fun postReissue(@Header("refresh_token") token: String): Call<ResponseBody>

    @GET("api/v1/terms/summary")
    fun getTerms(@Query("termsUsage") termsUsage: String): Call<ResponseBody>

    @GET("api/v1/terms/{id}")
    fun getTermDetails(@Path("id") userKey: String): Call<ResponseBody>

    @POST("api/v1/users/me/terms/agree")
    fun postTermsAgree(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/users/me/terms/agreed-status")
    fun getTermsAgree(@Header("Authorization") token: String, @Query("termsUsage") termsUsage: String, @Query("required") required: Boolean): Call<ResponseBody>

    @POST("api/v1/users/me/personal-cars")
    fun postMyCar(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/cars/registration-records")
    fun getCarInfoInquiry(@Header("Authorization") token: String, @Query("licensePlateNumber") licensePlateNumber: String, @Query("ownerName") ownerName: String): Call<ResponseBody>

    @PATCH("api/v1/users/me/personal-cars/{personalCarId}")
    fun patchCarInfoByCarId(@Header("Authorization") token: String, @Path("personalCarId") personalCarId: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/users/me/personal-cars")
    fun getMyCarInfo(@Header("Authorization") token: String): Call<ResponseBody>

    @GET("api/v1/users/me/personal-cars/{personalCarId}")
    fun getCarInfoinquiryByCarId(@Header("Authorization") token: String, @Path("personalCarId") personalCarId: String): Call<ResponseBody>

    @GET("api/v1/users/me/personal-cars/{personalCarId}")
    fun deleteMyCarByCarId(@Header("Authorization") token: String, @Path("personalCarId") personalCarId: String): Call<ResponseBody>

}