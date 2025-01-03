package com.milelog.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.PostMyCarResponse
import com.milelog.viewmodel.state.GetCarInfoInquiryState
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadCarInfoViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _setCarInfoInquiry = MutableLiveData<Event<GetCarInfoInquiryState>>()
    val setCarInfoInquiry: MutableLiveData<Event<GetCarInfoInquiryState>> get() = _setCarInfoInquiry


    fun init(context:Context){
        this.context = context
    }

    fun getCarInfoInquiry(carNo:String, carOwner:String){
        apiService(context,60).getCarInfoInquiry("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, carNo!!, carOwner!!).enqueue(object :
            Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    response.body()?.let{
                        _setCarInfoInquiry.value = Event(
                            GetCarInfoInquiryState.Success(it.string())
                        )
                    }?:{
                        _setCarInfoInquiry.value = Event(
                            GetCarInfoInquiryState.Empty
                        )
                    }
                }else if(response.code() == 401){
                    _setCarInfoInquiry.value = Event(
                        GetCarInfoInquiryState.Error(401, "")
                    )
                } else{
                    _setCarInfoInquiry.value = Event(
                        GetCarInfoInquiryState.Error(response.code(), response.message())
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}