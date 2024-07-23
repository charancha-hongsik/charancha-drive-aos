package com.charancha.drive.retrofit

import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServiceInterface {
    @POST("api/v1/cars/user-cars/drivings")
    fun postDrivingInfo(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/cars/-/user-cars/-/drivings/{drivingId}")
    fun getDrivingInfo(@Header("Authorization") token: String, @Path("drivingId") drivingId: String): Call<ResponseBody>


    @GET("api/v1/me/cars/-/user-cars/-/drivings")
    fun getDrivingHistories(@Header("Authorization") token: String,
                            @Query("size") size: Int,
                            @Query("order") order: String,
                            @Query("afterCursor") afterCursor: String?,
                            @Query("beforeCursor") beforeCursor: String?,
                            @Query("key") key: String,
                            @Query("startTime") startTime: String,
                            @Query("endTime") endTime: String): Call<ResponseBody>

    // 주행 기록 수정
    @PATCH("api/v1/cars/user-cars/drivings/{drivingId}")
    fun patchDrivingInfo(@Header("Authorization") token: String, @Path("drivingId") drivingId: String,@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/signup")
    fun postSignUp(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/signin")
    fun postSignIn(@Body body: RequestBody): Call<ResponseBody>

    @POST("api/v1/auth/token/reissue")
    fun postReissue(@Header("refresh_token") token: String): Call<ResponseBody>

    @GET("api/v1/terms")
    fun getTerms(@Query("usageType") termsUsage: String): Call<ResponseBody>

    @GET("api/v1/terms/{id}")
    fun getTermDetails(@Path("id") userKey: String): Call<ResponseBody>

    @PUT("api/v1/me/terms/agreements")
    fun postTermsAgree(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/me/terms/agreements")
    fun getTermsAgree(@Header("Authorization") token: String, @Query("termsUsage") termsUsage: String): Call<ResponseBody>


    /**
     * 자동차 등록 관련
     */
    // 자동차등록원부 조회
    @GET("api/v1/cars/registration-records")
    fun getCarInfoInquiry(@Header("Authorization") token: String, @Query("licensePlateNumber") licensePlateNumber: String, @Query("ownerName") ownerName: String): Call<ResponseBody>

    // 내가 등록한 개인 차량 정보 조회
    @GET("api/v1/me/cars/-/user-cars/{userCarId}")
    fun getCarInfoinquiryByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String): Call<ResponseBody>

    // 내 개인 차량 수정
    @PATCH("api/v1/me/cars/user-cars/{userCarId}")
    fun patchCarInfoByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String, @Body body: RequestBody): Call<ResponseBody>

    // 내 개인 차량 삭제
    @DELETE("api/v1/me/cars/user-cars/{userCarId}")
    fun deleteMyCarByCarId(@Header("Authorization") token: String, @Path("userCarId") personalCarId: String): Call<ResponseBody>

    // 내가 등록한 개인 차량 목록 조회
    @GET("api/v1/me/cars/-/user-cars")
    fun getMyCarInfo(@Header("Authorization") token: String): Call<ResponseBody>

    // 내 개인 차량 등록
    @POST("/api/v1/me/cars/user-cars")
    fun postMyCar(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    /**
     * 유저 관련
     */

    // 회원 탈퇴
    @DELETE("api/v1/me")
    fun deleteAccount(@Header("Authorization") token: String): Call<ResponseBody>


    // 사용자 조회
    @GET("api/v1/me")
    fun getAccount(@Header("Authorization") token: String): Call<ResponseBody>

    // 프로필 조회
    @GET("api/v1/me/profiles")
    fun getAccountProfiles(@Header("Authorization") token: String): Call<ResponseBody>

    // 프로필 업데이트
    @PATCH("api/v1/me/profiles")
    fun patchAccountProfiles(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    /**
     * 지표 상세
     */
    // 주행 통계 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics")
    fun getDrivingStatistics(@Header("Authorization") token: String,
                           @Path("userCarId") userCarId: String,
                           @Query("criteriaStartTime") criteriaStartTime: String,
                           @Query("criteriaEndTime") criteriaEndTime: String,
                           @Query("criteriaTime") criteriaTime: String,
                           @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>


    // 최근 주행 통계 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/recent")
    fun getRecentDrivingStatistics(@Header("Authorization") token: String,
                           @Path("userCarId") userCarId: String): Call<ResponseBody>


    // 주행 거리 그래프 데이터 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/driving-distance/graph")
    fun getDrivingDistanceGraphData(@Header("Authorization") token: String,
                          @Path("userCarId") userCarId: String,
                          @Query("order") order: String,
                          @Query("afterCursor") afterCursor: String?,
                          @Query("beforeCursor") beforeCursor: String?,
                          @Query("criteriaStartTime") criteriaStartTime: String,
                          @Query("criteriaEndTime") criteriaEndTime: String,
                          @Query("criteriaTime") criteriaTime: String,
                          @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>


    // 주행 거리 비율 그래프 데이터 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/driving-distance-ratio/graph")
    fun getDrivingDistanceRatioGraphData(@Header("Authorization") token: String,
                          @Path("userCarId") userCarId: String,
                          @Query("order") order: String,
                          @Query("afterCursor") afterCursor: String?,
                          @Query("beforeCursor") beforeCursor: String?,
                          @Query("criteriaStartTime") criteriaStartTime: String,
                          @Query("criteriaEndTime") criteriaEndTime: String,
                          @Query("criteriaTime") criteriaTime: String,
                          @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>

    // 1회 평균 주행 거리 그래프 데이터 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/driving-distance-per-one/graph")
    fun getDrivingDistancePerOneGraphData(@Header("Authorization") token: String,
                               @Path("userCarId") userCarId: String,
                               @Query("order") order: String,
                               @Query("afterCursor") afterCursor: String?,
                               @Query("beforeCursor") beforeCursor: String?,
                               @Query("criteriaStartTime") criteriaStartTime: String,
                               @Query("criteriaEndTime") criteriaEndTime: String,
                               @Query("criteriaTime") criteriaTime: String,
                               @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>

    // 주행 시간 그래프 데이터 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/driving-time/graph")
    fun getDrivingTimeGraphData(@Header("Authorization") token: String,
                                         @Path("userCarId") userCarId: String,
                                         @Query("order") order: String,
                                         @Query("afterCursor") afterCursor: String?,
                                         @Query("beforeCursor") beforeCursor: String?,
                                         @Query("criteriaStartTime") criteriaStartTime: String,
                                         @Query("criteriaEndTime") criteriaEndTime: String,
                                         @Query("criteriaTime") criteriaTime: String,
                                         @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>

    // 1회 평균 주행 시간 그래프 데이터 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings/-/statistics/driving-time/graph")
    fun getDrivingTimePerOneGraphData(@Header("Authorization") token: String,
                                @Path("userCarId") userCarId: String,
                                @Query("order") order: String,
                                @Query("afterCursor") afterCursor: String,
                                @Query("beforeCursor") beforeCursor: String,
                                @Query("criteriaStartTime") criteriaStartTime: String,
                                @Query("criteriaEndTime") criteriaEndTime: String,
                                @Query("criteriaTime") criteriaTime: String,
                                @Query("minimumTimeUnit") minimumTimeUnit: String): Call<ResponseBody>

    // 관리 점수 통계 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/score/-/statistics")
    fun getManageScoreStatistics(@Header("Authorization") token: String,
                                      @Path("userCarId") userCarId: String,
                                      @Query("startTime") order: String,
                                      @Query("endTime") afterCursor: String): Call<ResponseBody>

    // 최근 관리 점수 통계 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/score/-/statistics/recent")
    fun getRecentManageScoreStatistics(@Header("Authorization") token: String,
                                 @Path("userCarId") userCarId: String): Call<ResponseBody>

}