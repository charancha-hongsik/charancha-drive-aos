package com.milelog.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.GetLatestResponse
import com.milelog.retrofit.response.GetNotificationListsResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.AlarmEntity
import com.milelog.viewmodel.state.CheckForceUpdateState
import com.milelog.viewmodel.state.GetDriveHistoryMoreState

import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _checkForceUpdate = MutableLiveData<Event<CheckForceUpdateState>>()
    val checkForceUpdate: MutableLiveData<Event<CheckForceUpdateState>> get() = _checkForceUpdate

    fun init(context:Context){
        this.context = context

        viewModelScope.launch {

        }
    }

    fun checkForceUpdate(){
        apiService(context).getLatest("AOS","PHONE").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201) {
                    val getLatestResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetLatestResponse::class.java
                    )

                    try {
                        if(getLatestResponse.forceUpdate){
                            val currentAppVersion = BuildConfig.VERSION_NAME
                            val majorFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[0]
                            val minorFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[1]
                            val patchFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[2]
                            val major = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[0]
                            val minor = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1]
                            val patch = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[2]


                            if (patchFromApi.toInt() > patch.toInt()) {
                                _checkForceUpdate.value = Event(CheckForceUpdateState.Success(true))
                                return
                            }
                            if (minorFromApi.toInt() > minor.toInt()) {
                                _checkForceUpdate.value = Event(CheckForceUpdateState.Success(true))
                                return
                            }
                            if (majorFromApi.toInt() > major.toInt()) {
                                _checkForceUpdate.value = Event(CheckForceUpdateState.Success(true))
                                return
                            }
                            _checkForceUpdate.value = Event(CheckForceUpdateState.Success(false))
                        }else{
                            _checkForceUpdate.value = Event(CheckForceUpdateState.Success(false))
                        }


                    } catch (e: PackageManager.NameNotFoundException) {
                        _checkForceUpdate.value = Event(CheckForceUpdateState.Empty)
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _checkForceUpdate.value = Event(CheckForceUpdateState.Empty)
            }

        })
    }

}