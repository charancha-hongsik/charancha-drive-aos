package com.milelog.viewmodel

import WinRewardHistoryResponse
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
import com.milelog.viewmodel.state.GetWinRewardHistoryMoreState
import com.milelog.viewmodel.state.GetWinRewardHistoryState
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WinRewardHistoryViewModel: BaseViewModel() {
    lateinit var context: Context


    private val _winRewardHistoryMoreResult = MutableLiveData<Event<GetWinRewardHistoryMoreState>>()
    val winRewardHistoryMoreResult: MutableLiveData<Event<GetWinRewardHistoryMoreState>> get() = _winRewardHistoryMoreResult

    private val _winRewardHistoryResult = MutableLiveData<Event<GetWinRewardHistoryState>>()
    val winRewardHistoryResult: MutableLiveData<Event<GetWinRewardHistoryState>> get() = _winRewardHistoryResult


    fun init(context:Context){
        this.context = context
    }

    fun getHistoriesMore(){

        apiService(context).getWinRewardHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            page = 1,
            order = "DESC").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val winRewardHistoryResponse = GsonBuilder().serializeNulls().create().fromJson(
                        response.body()?.string(),
                        WinRewardHistoryResponse::class.java
                    )
                    _winRewardHistoryMoreResult.value = Event(GetWinRewardHistoryMoreState.Success(winRewardHistoryResponse))
                }else{
                    _winRewardHistoryMoreResult.value = Event(GetWinRewardHistoryMoreState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getHistories(){
        apiService(context).getWinRewardHistories(
            token = "Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            page = 1,
            order = "DESC").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()
                    Log.d("testsetestest","testestesestset jsonString :: " + jsonString)
                    val winRewardHistoryResponse = GsonBuilder().serializeNulls().create().fromJson(
                        jsonString,
                        WinRewardHistoryResponse::class.java
                    )

                    _winRewardHistoryResult.value = Event(GetWinRewardHistoryState.Success(winRewardHistoryResponse))

                } else{

                    _winRewardHistoryResult.value = Event(GetWinRewardHistoryState.Error(response.code(), response.message()))

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }



}