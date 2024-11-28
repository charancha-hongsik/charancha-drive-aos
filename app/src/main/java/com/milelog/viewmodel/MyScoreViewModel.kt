package com.milelog.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.milelog.BoundingBoxCalculator
import com.milelog.CommonUtil
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.Address
import com.milelog.retrofit.request.Parcel
import com.milelog.retrofit.request.Place
import com.milelog.retrofit.request.PlaceAddress
import com.milelog.retrofit.request.Point
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.request.Road
import com.milelog.retrofit.response.GetAccountResponse
import com.milelog.retrofit.response.GetDrivingStatisticsResponse
import com.milelog.retrofit.response.GetManageScoreResponse
import com.milelog.retrofit.response.GetMyCarInfoItem
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.PostDrivingInfoResponse
import com.milelog.retrofit.response.VWorldDetailResponse
import com.milelog.retrofit.response.VWorldResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApi
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.CarInfoInquiryByCarIdState
import com.milelog.viewmodel.state.GetDrivingStatisticsState
import com.milelog.viewmodel.state.GetManageScoreState
import com.milelog.viewmodel.state.MyCarInfoState
import com.milelog.viewmodel.state.NotSavedDataState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class MyScoreViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _accountResult = MutableLiveData<Event<AccountState>>()
    val accountResult: MutableLiveData<Event<AccountState>> get() = _accountResult

    private val _notSavedDataResult = MutableLiveData<Event<NotSavedDataState>>()
    val notSavedDataStateResult: MutableLiveData<Event<NotSavedDataState>> get() = _notSavedDataResult

    private val _myCarInfoResult = MutableLiveData<Event<MyCarInfoState>>()
    val myCarInfoResult: MutableLiveData<Event<MyCarInfoState>> get() = _myCarInfoResult

    private val _carInfoInquiryByCarId = MutableLiveData<Event<CarInfoInquiryByCarIdState>>()
    val carInfoInquiryByCarId: MutableLiveData<Event<CarInfoInquiryByCarIdState>> get() = _carInfoInquiryByCarId

    private val _managerScoreResult = MutableLiveData<Event<GetManageScoreState>>()
    val managerScoreResult: MutableLiveData<Event<GetManageScoreState>> get() = _managerScoreResult

    private val _drivingStatisticsResult = MutableLiveData<Event<GetDrivingStatisticsState>>()
    val drivingStatisticsResult: MutableLiveData<Event<GetDrivingStatisticsState>> get() = _drivingStatisticsResult

    private val _recentManageScoreResult = MutableLiveData<Event<GetManageScoreState>>()
    val recentManageScoreResult: MutableLiveData<Event<GetManageScoreState>> get() = _recentManageScoreResult

    private val _manageScoreForSummaryResult = MutableLiveData<Event<GetManageScoreState>>()
    val manageScoreForSummaryResult: MutableLiveData<Event<GetManageScoreState>> get() = _manageScoreForSummaryResult

    fun init(context:Context){
        this.context = context
    }

    fun getAccount(){
        apiService(context).getAccount("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                when {

                    response.code() == 200 || response.code() == 201 -> {
                        val getAccountResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetAccountResponse::class.java
                        )
                        _accountResult.value = Event(AccountState.Success(getAccountResponse))
                    }
                    else -> {
                        _accountResult.value = Event(AccountState.Error(response.code(), response.message()))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun postDrivingInfoNotSavedData(){
        if(isInternetConnected(context)){
            Executors.newSingleThreadExecutor().execute {
                val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
                driveDatabase.driveForApiDao().allDriveLimit5?.let {
                    if (it.isNotEmpty()) {
                        for (drive in it) {
                            val startPoint  = drive.startPoint!!.x + "," + drive.startPoint!!.y
                            val endPoint  = drive.endPoint!!.x + "," + drive.endPoint!!.y

                            val startBbox = BoundingBoxCalculator.getBoundingBox(
                                BoundingBoxCalculator.MapPoint(drive.startPoint!!.x.toDouble(), drive.startPoint!!.y.toDouble()),0.02)
                            val endBbox = BoundingBoxCalculator.getBoundingBox(BoundingBoxCalculator.MapPoint(drive.endPoint!!.x.toDouble(), drive.endPoint!!.y.toDouble()),0.02)

                            val startBboxPoint = startBbox.minPoint.longitude.toString() + "," + startBbox.minPoint.latitude.toString() + "," + startBbox.maxPoint.longitude.toString() + "," + startBbox.maxPoint.latitude.toString()
                            val endBboxPoint = endBbox.minPoint.longitude.toString() + "," + endBbox.minPoint.latitude.toString() + "," + endBbox.maxPoint.longitude.toString() + "," + endBbox.maxPoint.latitude.toString()

                            var startAddress = Address(Point(drive.startPoint.x, drive.startPoint.y),null,null,null)
                            var endAddress = Address(Point(drive.endPoint.x, drive.endPoint.y),null,null,null)

                            /**
                             * startAddress
                             */
                            CommonUtil.apiService(
                                context,
                                30,
                                "https://api.vworld.kr/"
                            ).getAddress(point = startPoint).enqueue(object:
                                Callback<ResponseBody>{
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>
                                ) {
                                    val vWorldResponse = GsonBuilder().serializeNulls().create().fromJson(
                                        response.body()?.string(),
                                        VWorldResponse::class.java
                                    )

                                    val level4 = vWorldResponse.response.result.first().structure.level4L
                                        ?: vWorldResponse.response.result.first().structure.level4LC
                                        ?: vWorldResponse.response.result.first().structure.level4A
                                        ?: vWorldResponse.response.result.first().structure.level4AC

                                    startAddress.parcel = vWorldResponse.response.result.find { it.type == "parcel" }?.text?.let { Parcel(it) }
                                    startAddress.road = vWorldResponse.response.result.find { it.type == "road" }?.text?.let { Road(it) }


                                    CommonUtil.apiService(
                                        context,
                                        30,
                                        "https://api.vworld.kr/"
                                    ).getAddressDetail(query = level4, bbox = startBboxPoint).enqueue(object:
                                        Callback<ResponseBody>{
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            val jsonString = response.body()?.string()
                                            val vWorldDetailResponse = GsonBuilder().serializeNulls().create().fromJson(
                                                jsonString,
                                                VWorldDetailResponse::class.java
                                            )

                                            if(vWorldDetailResponse.response.status != "NOT_FOUND"){

                                                var places:MutableList<Place> = mutableListOf()
                                                for(detail in vWorldDetailResponse.response.result.items){
                                                    places.add(
                                                        Place(detail.category, detail.title, Point(detail.point.x, detail.point.y), PlaceAddress(
                                                            Road(detail.address.road), Parcel(detail.address.parcel)
                                                        )
                                                        )
                                                    )
                                                }

                                                startAddress.places = places
                                            }

                                            // endAddress api 시작
                                            callGetEndAddress(startAddress, endAddress, drive, driveDatabase, endPoint, endBboxPoint)
                                        }

                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                            // endAddress api 시작
                                            /**
                                             * endAddress
                                             */
                                            callGetEndAddress(startAddress, endAddress, drive, driveDatabase, endPoint, endBboxPoint)
                                        }
                                    })
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    callGetEndAddress(startAddress, endAddress, drive, driveDatabase, endPoint, endBboxPoint)
                                }
                            })
                        }
                    } else {

                    }
                }
            }

        }else{

        }
    }

    fun getMyCarInfo(){
        apiService(context).getMyCarInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()
                    val getMyCarInfoResponses = GsonBuilder().serializeNulls().create().fromJson(jsonString,GetMyCarInfoResponse::class.java)

                    _myCarInfoResult.value = Event(MyCarInfoState.Success(getMyCarInfoResponses))

                }else{
                    _myCarInfoResult.value = Event(MyCarInfoState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {

            }
        })
    }

    fun getCarInfoinquiryByCarId(id:String){
        apiService(context).getCarInfoinquiryByCarId("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, id).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.d("testestestst","testesestestes response.code :: " + response.code())

                if(response.code() == 200 || response.code() == 201){
                    val getMyCarInfoResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        GetMyCarInfoItem::class.java
                    )

                    _carInfoInquiryByCarId.value = Event(CarInfoInquiryByCarIdState.Success(getMyCarInfoResponse))

                }else if(response.code() == 401){
                    _carInfoInquiryByCarId.value = Event(CarInfoInquiryByCarIdState.Error(response.code(), response.message()))

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun getManageScoreForAMonth(userCarId:String){
        apiService(context).getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("testestestst","testesestestes response.code :: " + response.code())
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetManageScoreResponse::class.java
                        )
                        _managerScoreResult.value = Event(GetManageScoreState.Success(getManageScoreResponse))
                    }else{
                        _managerScoreResult.value = Event(GetManageScoreState.Error(response.code(), response.message()))

                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun getDrivingDistanceForAMonth(userCarId:String){
        apiService(context).getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day").enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("testestestst","testesestestes response.code :: " + response.code())

                    if (response.code() == 200 || response.code() == 201) {
                        val getDrivingStatisticsResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetDrivingStatisticsResponse::class.java
                        )
                        _drivingStatisticsResult.value = Event(GetDrivingStatisticsState.Success(getDrivingStatisticsResponse))
                    } else {
                        _drivingStatisticsResult.value = Event(GetDrivingStatisticsState.Error(response.code(), response.message()))

                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setRecentManageScoreForSummary(userCarId:String){
        apiService(context).getRecentManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId
        ).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetManageScoreResponse::class.java
                        )
                        _recentManageScoreResult.value = Event(GetManageScoreState.Success(getManageScoreResponse))
                    } else {
                        _recentManageScoreResult.value = Event(GetManageScoreState.Error(response.code(), response.message()))

                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setManageSoreForSummary(scope:Long, userCarId:String){
        apiService(context).getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetManageScoreResponse::class.java
                        )
                        _manageScoreForSummaryResult.value = Event(GetManageScoreState.Success(getManageScoreResponse))
                    }else{
                        _manageScoreForSummaryResult.value = Event(GetManageScoreState.Error(response.code(), response.message()))

                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    private fun callSaveDriving(startAddress:Address, endAddress: Address, drive:DriveForApi, driveDatabase: DriveDatabase){
        // 내 주행 저장 API 호출
        val postDrivingInfoRequest = PostDrivingInfoRequest(
            userCarId = drive.userCarId,
            startTimestamp = drive.startTimestamp,
            endTimestamp = drive.endTimestamp,
            verification = drive.verification,
            gpses = drive.gpses,
            startAddress,
            endAddress
        )

        val gson = GsonBuilder().serializeNulls().create()
        val jsonParam = gson.toJson(postDrivingInfoRequest)

        apiService(context).postMyDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull()))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    try {
                        if (response.code() == 200 || response.code() == 201) {
                            val postDrivingInfoResponse = gson.fromJson(
                                response.body()?.string(),
                                PostDrivingInfoResponse::class.java
                            )
                            // 보낸 데이터 삭제
                            driveDatabase.driveForApiDao()
                                .deleteByTrackingId(drive.tracking_id)

                            // DriveForApp tracking_id 저장
                            driveDatabase.driveForAppDao()
                                .updateTrackingId(
                                    drive.tracking_id,
                                    postDrivingInfoResponse.id
                                )
                        } else if(response.code() == 401){
                            _notSavedDataResult.value = Event(NotSavedDataState.Error(response.code(), response.message()))
                        } else if(response.code() == 429){
                            // 보낸 데이터 삭제
                            driveDatabase.driveForApiDao()
                                .deleteByTrackingId(drive.tracking_id)
                        }

                    }catch (e:Exception){

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {


                }
            })
    }

    private fun callGetEndAddress(startAddress:Address, endAddress: Address, drive:DriveForApi,  driveDatabase: DriveDatabase, endPoint:String,endBboxPoint:String ){
        // endAddress api 시작
        /**
         * endAddress
         */
        CommonUtil.apiService(context, 30, "https://api.vworld.kr/").getAddress(point = endPoint).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                val vWorldResponse = GsonBuilder().serializeNulls().create().fromJson(
                    response.body()?.string(),
                    VWorldResponse::class.java
                )

                endAddress.parcel = vWorldResponse.response.result.find { it.type == "parcel" }?.text?.let { Parcel(it) }
                endAddress.road = vWorldResponse.response.result.find { it.type == "road" }?.text?.let { Road(it) }

                val level4 = vWorldResponse.response.result.first().structure.level4L
                    ?: vWorldResponse.response.result.first().structure.level4LC
                    ?: vWorldResponse.response.result.first().structure.level4A
                    ?: vWorldResponse.response.result.first().structure.level4AC

                CommonUtil.apiService(context, 30, "https://api.vworld.kr/")
                    .getAddressDetail(query = level4, bbox = endBboxPoint).enqueue(object:
                    Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        val vWorldDetailResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            VWorldDetailResponse::class.java
                        )

                        if(vWorldDetailResponse.response.status != "NOT_FOUND"){
                            var places:MutableList<Place> = mutableListOf()
                            for(detail in vWorldDetailResponse.response.result.items){
                                places.add(
                                    Place(detail.category, detail.title, Point(detail.point.x, detail.point.y), PlaceAddress(
                                        Road(detail.address.road), Parcel(detail.address.parcel)
                                    )
                                    )
                                )
                            }

                            endAddress.places = places

                            // 내 주행 저장 API 호출
                            callSaveDriving(startAddress, endAddress, drive, driveDatabase)
                        }else{
                            callSaveDriving(startAddress, endAddress, drive, driveDatabase)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callSaveDriving(startAddress, endAddress, drive, driveDatabase)
                    }
                })
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callSaveDriving(startAddress, endAddress, drive, driveDatabase)
            }
        })
    }
}