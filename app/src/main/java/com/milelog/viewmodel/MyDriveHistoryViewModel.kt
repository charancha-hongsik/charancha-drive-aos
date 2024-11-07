package com.milelog.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.DriveItem
import com.milelog.retrofit.response.GetDriveHistoryResponse
import com.milelog.retrofit.response.Meta
import com.milelog.viewmodel.state.GetDriveHistoryMoreState
import com.milelog.viewmodel.state.GetDriveHistoryState
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyDriveHistoryViewModel: BaseViewModel() {
    lateinit var context: Context


    private val _driveHistoryMoreResult = MutableLiveData<Event<GetDriveHistoryMoreState>>()
    val driveHistoryMoreResult: MutableLiveData<Event<GetDriveHistoryMoreState>> get() = _driveHistoryMoreResult

    private val _driveHistoryResult = MutableLiveData<Event<GetDriveHistoryState>>()
    val driveHistoryResult: MutableLiveData<Event<GetDriveHistoryState>> get() = _driveHistoryResult


    fun init(context:Context){
        this.context = context
    }

    fun getHistoriesMore(startTime:String, endTime:String, meta: Meta, histories: MutableList<DriveItem>){
        apiService(context).getDrivingHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            size = 30,
            order = "DESC",
            afterCursor =  meta.afterCursor,
            beforeCursor = null,
            key = "startTime",
            startTime = startTime,
            endTime = endTime,
            isActive = null,
            userCarId = null).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getDriveHistroyResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )
                    meta.afterCursor = getDriveHistroyResponse.meta.afterCursor
                    _driveHistoryMoreResult.value = Event(GetDriveHistoryMoreState.Success(getDriveHistroyResponse))
                }else{
                    _driveHistoryMoreResult.value = Event(GetDriveHistoryMoreState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getHistories(startTime:String, endTime:String){
        apiService(context).getDrivingHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            size = 30,
            order = "DESC",
            afterCursor =  null,
            beforeCursor = null,
            key = "startTime",
            startTime = startTime,
            endTime = endTime,
            isActive = null,
            userCarId = null).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getDriveHistroyResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )
                    _driveHistoryResult.value = Event(GetDriveHistoryState.Success(getDriveHistroyResponse, startTime, endTime))

                } else{
                    _driveHistoryResult.value = Event(GetDriveHistoryState.Error(response.code(), response.message()))

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }



}