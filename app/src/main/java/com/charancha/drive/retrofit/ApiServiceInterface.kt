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


    /**
     * 자동차 등록 관련
     */
    // 자동차등록원부 조회
    @GET("api/v1/cars/registration-records")
    fun getCarInfoInquiry(@Header("Authorization") token: String, @Query("licensePlateNumber") licensePlateNumber: String, @Query("ownerName") ownerName: String): Call<ResponseBody>

    // 내가 등록한 개인 차량 정보 조회
    @GET("api/v1/users/me/cars/user-cars/{userCarId}")
    fun getCarInfoinquiryByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String): Call<ResponseBody>

    // 내 개인 차량 수정
    @PATCH("api/v1/users/me/cars/user-cars/{userCarId}")
    fun patchCarInfoByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String, @Body body: RequestBody): Call<ResponseBody>

    // 내 개인 차량 삭제
    @DELETE("api/v1/users/me/cars/user-cars/{userCarId}")
    fun deleteMyCarByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String): Call<ResponseBody>

    // 내가 등록한 개인 차량 목록 조회
    @GET("api/v1/users/me/cars/user-cars")
    fun getMyCarInfo(@Header("Authorization") token: String): Call<ResponseBody>

    // 내 개인 차량 등록
    @POST("api/v1/users/me/cars/user-cars")
    fun postMyCar(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>


}