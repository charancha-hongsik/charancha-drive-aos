package com.milelog.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.response.GetAccountResponse
import com.milelog.retrofit.response.GetDrivingStatisticsResponse
import com.milelog.retrofit.response.GetManageScoreResponse
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.PostDrivingInfoResponse
import com.milelog.room.database.DriveDatabase
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
import java.lang.reflect.Type
import java.util.concurrent.Executors

class MainViewModel: BaseViewModel() {
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
                        val getAccountResponse = Gson().fromJson(
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
                            val postDrivingInfoRequest = PostDrivingInfoRequest(
                                userCarId = PreferenceUtil.getPref(context, PreferenceUtil.USER_CARID, "")!!,
                                startTimestamp = drive.startTimestamp,
                                endTimestamp = drive.endTimestamp,
                                verification = drive.verification,
                                gpses = drive.gpses
                            )

                            val gson = Gson()
                            val jsonParam = gson.toJson(postDrivingInfoRequest)

                            apiService(context).postDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull()))
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

                    val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                    val getMyCarInfoResponses:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

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
                if(response.code() == 200 || response.code() == 201){
                    val getMyCarInfoResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetMyCarInfoResponse::class.java
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

    fun getManageScoreForAMonth(){
        apiService(context).getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(context, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = Gson().fromJson(
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

    fun getDrivingDistanceForAMonth(){
        apiService(context).getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(context, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day").enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getDrivingStatisticsResponse = Gson().fromJson(
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

    fun setRecentManageScoreForSummary(){
        apiService(context).getRecentManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(context, PreferenceUtil.USER_CARID, "")!!
        ).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = Gson().fromJson(
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

    fun setManageSoreForSummary(scope:Long){
        apiService(context).getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(context, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getManageScoreResponse = Gson().fromJson(
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
}