package com.milelog.viewmodel

import android.content.Context

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.GetAccountResponse
import com.milelog.viewmodel.state.AccountState
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _accountResult = MutableLiveData<Event<AccountState>>()
    val accountResult: MutableLiveData<Event<AccountState>> get() = _accountResult

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
}