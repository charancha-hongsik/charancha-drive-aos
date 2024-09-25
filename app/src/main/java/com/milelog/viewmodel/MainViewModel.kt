package com.milelog.viewmodel

import android.content.Context

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.response.GetAccountResponse
import com.milelog.retrofit.response.PostDrivingInfoResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.NotSavedDataState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class MainViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _accountResult = MutableLiveData<Event<AccountState>>()
    val accountResult: MutableLiveData<Event<AccountState>> get() = _accountResult

    private val _notSavedDataResult = MutableLiveData<Event<NotSavedDataState>>()
    val notSavedDataStateResult: MutableLiveData<Event<NotSavedDataState>> get() = _notSavedDataResult

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
                                                // update id drive.
                                                // tracking_id to postDrivingInfoResponse.id

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
}