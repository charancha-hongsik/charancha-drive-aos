package com.milelog.viewmodel

import android.content.Context
import android.util.Log
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

class WinRewardHistoryViewModel: BaseViewModel() {
    lateinit var context: Context


    private val _winRewardHistoryMoreResult = MutableLiveData<Event<GetDriveHistoryMoreState>>()
    val winRewardHistoryMoreResult: MutableLiveData<Event<GetDriveHistoryMoreState>> get() = _winRewardHistoryMoreResult

    private val _winRewardHistoryResult = MutableLiveData<Event<GetDriveHistoryState>>()
    val winRewardHistoryResult: MutableLiveData<Event<GetDriveHistoryState>> get() = _winRewardHistoryResult


    fun init(context:Context){
        this.context = context
    }

    fun getHistoriesMore(startTime:String, endTime:String, meta: Meta, isActive:Boolean? = null, userCarId:String? = null){

        apiService(context).getWinRewardHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            size = 30,
            order = "DESC").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getDriveHistroyResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )
                    meta.afterCursor = getDriveHistroyResponse.meta.afterCursor
                    _winRewardHistoryMoreResult.value = Event(GetDriveHistoryMoreState.Success(getDriveHistroyResponse))
                }else{
                    _winRewardHistoryMoreResult.value = Event(GetDriveHistoryMoreState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getHistories(startTime:String, endTime:String,isActive:Boolean? = null, userCarId:String?){

        apiService(context).getWinRewardHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            size = 30,
            order = "DESC").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()
                    val getDriveHistroyResponse = GsonBuilder().serializeNulls().create().fromJson(
                        jsonString,
                        GetDriveHistoryResponse::class.java
                    )

                    _winRewardHistoryResult.value = Event(GetDriveHistoryState.Success(getDriveHistroyResponse, startTime, endTime))

                } else{
                    _winRewardHistoryResult.value = Event(GetDriveHistoryState.Error(response.code(), response.message()))

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }



}