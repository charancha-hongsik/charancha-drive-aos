package com.milelog.retrofit

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServiceInterface {

    // 내 주행 생성
    @POST("api/v1/me/cars/user-cars/drivings")
    fun postMyDrivingInfo(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>


    /**
     * 전체 조회: 필터 사용 X
     * 미확인 : isActive = true
     * 특정유저차량: isActive = true, userCarId = 특정유저차량ID
     * 내 차가 아니에요: isActive = false
     */

    // 내 주행 목록 조회
    @GET("api/v1/me/cars/-/user-cars/-/drivings")
    fun getDrivingHistories(@Header("Authorization") token: String,
                            @Query("size") size: Int,
                            @Query("order") order: String,
                            @Query("afterCursor") afterCursor: String?,
                            @Query("beforeCursor") beforeCursor: String?,
                            @Query("key") key: String,
                            @Query("isActive") isActive: Boolean?,
                            @Query("startTime") startTime: String,
                            @Query("endTime") endTime: String,
                            @Query("userCarId") userCarId: String?): Call<ResponseBody>

    // 내 주행 목록 상세 조회
    @GET("api/v1/me/cars/-/user-cars/-/drivings/-/detail")
    fun getDrivingDetailHistories(@Header("Authorization") token: String,
                            @Query("size") size: Int,
                            @Query("order") order: String,
                            @Query("afterCursor") afterCursor: String?,
                            @Query("beforeCursor") beforeCursor: String?,
                            @Query("key") key: String,
                            @Query("isActive") isActive: Boolean?,
                            @Query("startTime") startTime: String,
                            @Query("endTime") endTime: String,
                            @Query("userCarId") userCarId: String?): Call<ResponseBody>

    // 특정 유저차량의 주행목록 조회
    @GET("api/v1/cars/-/user-cars/{userCarId}/drivings")
    fun getDrivingInfoByUserCarId(@Header("Authorization") token: String, @Path("userCarId") userCarId: String): Call<ResponseBody>

    // 주행 상세 조회
    @GET("api/v1/cars/-/user-cars/-/drivings/{drivingId}")
    fun getDrivingInfo(@Header("Authorization") token: String, @Path("drivingId") drivingId: String): Call<ResponseBody>



    @Multipart
    @PATCH("api/v1/cars/user-cars/drivings/{drivingId}")
    fun patchDrivingInfo(
        @Header("Authorization") token: String,
        @Path("drivingId") drivingId: String,
        @Part("data") data: RequestBody?,
        @Part images: List<MultipartBody.Part>?
    ): Call<ResponseBody>

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
    @GET("api/v1/cars/-/infoinquiry")
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
    fun getMyCarInfo(
        @Header("Authorization") token: String,
        @Query("size") size: Int? = null,
        @Query("order") order: String? = null,
        @Query("afterCursor") afterCursor: String? = null,
        @Query("beforeCursor") beforeCursor: String? = null,
        @Query("key") key: String? = null,
        @Query("makerCd") makerCd: String? = null,
        @Query("modelCd") modelCd: String? = null,
        @Query("modelDetailCd") modelDetailCd: String? = null,
        @Query("gradeCd") gradeCd: String? = null,
        @Query("gradeDetailCd") gradeDetailCd: String? = null,
        @Query("fuelCd") fuelCd: String? = null,
    ): Call<ResponseBody>

    // 내 개인 차량 등록
    @POST("api/v1/me/cars/user-cars")
    fun postMyCar(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    // 차란차 코드 조회 API
    @GET("api/v1/cars/-/charancha-codes/{key}")
    fun getCharanchaCode(@Header("Authorization") token: String, @Path("key") key: String, @Query("parentCode") parentCode: String?): Call<ResponseBody>

    // 유저차량 수 조회 API
    @GET("api/v1/me/cars/-/user-cars/-/count")
    fun getMyCarCount(
        @Header("Authorization") token: String,
        @Query("vehicleIdentificationNumber") vehicleIdentificationNumber: String? = null,
        @Query("makerCd") makerCd: String? = null,
        @Query("modelCd") modelCd: String? = null,
        @Query("modelDetailCd") modelDetailCd: String? = null,
        @Query("gradeCd") gradeCd: String? = null,
        @Query("gradeDetailCd") gradeDetailCd: String? = null,
        @Query("fuelCd") fuelCd: String? = null): Call<ResponseBody>

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
    @Multipart
    @PATCH("api/v1/me/profiles")
    fun patchAccountProfiles(@Header("Authorization") token: String, @Part("nickName") nickName: RequestBody?, @Part("imageUpdateType")  imageUpdateType: RequestBody, @Part image:MultipartBody.Part?): Call<ResponseBody>

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

    // 강업 버전 조회
    @GET("api/v1/apps/-/latest")
    fun getLatest(@Query("os") os: String, @Query("deviceType") deviceType: String): Call<ResponseBody>

    // 디바이스 정보 저장
    @POST("api/v1/devices")
    fun postDeviceInfo(@Body body: RequestBody): Call<ResponseBody>

    // 디바이스 정보 수정
    @PATCH("api/v1/devices/{deviceId}")
    fun patchDeviceInfo(@Path("deviceId") deviceId: String, @Body body: RequestBody): Call<ResponseBody>

    // 내 디바이스로 연결
    @POST("api/v1/me/devices/connect")
    fun postConnectDevice(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>


    // 내 디바이스로 해제
    @POST("api/v1/me/devices/disconnect")
    fun postDisconnectDevice(@Header("Authorization") token: String, @Body body: RequestBody): Call<ResponseBody>

    @GET("api/v1/notifications")
    fun getNotificationLists(@Header("Authorization") token: String,
                            @Query("size") size: Int,
                            @Query("order") order: String,
                            @Query("afterCursor") afterCursor: String?,
                            @Query("beforeCursor") beforeCursor: String?,
                            @Query("name") name: String?,
                            @Query("isActive") isActive: Boolean?): Call<ResponseBody>

    @GET("api/v1/me/notifications/-/agreements")
    fun getMyNotificationAgreed(@Header("Authorization") token: String,
                             @Query("size") size: Int,
                             @Query("order") order: String,
                             @Query("afterCursor") afterCursor: String?,
                             @Query("beforeCursor") beforeCursor: String?,
                             @Query("isAgreed") isAgreed: Boolean?): Call<ResponseBody>


    @PUT("api/v1/me/notifications/agreements")
    fun putMyNotificationAgreed(@Header("Authorization") token: String,
                                @Body body: RequestBody): Call<ResponseBody>


    /**
     * VWorld 역지오
     */



    @GET("req/address")
    fun getAddress(@Query("service") service: String = "address",
                   @Query("request") request: String = "getAddress",
                   @Query("version") version: String = "2.0",
                   @Query("crs") crs: String = "epsg:4326",
                   @Query("point") point: String,
                   @Query("format") format: String = "json",
                   @Query("type") type: String = "both",
                   @Query("zipcode") zipcode: Boolean = true,
                   @Query("simple") simple: Boolean = false,
                   @Query("key") key: String = "FA9E1CC5-49CC-345E-A27E-18266F94C2A7"):Call<ResponseBody>

    @GET("req/search")
    fun getAddressDetail(@Query("service") service: String = "search",
                   @Query("request") request: String = "search",
                   @Query("version") version: String = "2.0",
                   @Query("size") size: Int = 10,
                   @Query("page") page: Int = 1,
                   @Query("query") query: String,
                   @Query("bbox") bbox: String,
                   @Query("type") type: String = "place",
                   @Query("format") format: String = "json",
                   @Query("errorformat") errorformat: String = "json",
                   @Query("key") key: String = "FA9E1CC5-49CC-345E-A27E-18266F94C2A7"):Call<ResponseBody>

    @GET("api/v1/me/rewards/boxes/results")
    fun getWinRewardHistories(@Header("Authorization") token: String,
                                @Query("page") size: Int? = 1,
                                @Query("limit") limit: Int? = 30,
                                @Query("order") order: String? = "DESC",
                                @Query("sort") sort: String? = "createdAt",
                                @Query("filters") filters: String? = "",
                                @Query("custom-filters") customFilters: String? = ""): Call<ResponseBody>



}