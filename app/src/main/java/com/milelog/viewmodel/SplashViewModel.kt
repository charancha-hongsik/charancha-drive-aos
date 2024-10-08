package com.milelog.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.activity.LoginActivity
import com.milelog.activity.MainActivity
import com.milelog.activity.OnBoardingActivity
import com.milelog.activity.PermissionInfoActivity
import com.milelog.activity.TermsOfUseActivity
import com.milelog.retrofit.response.GetLatestResponse
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.GetNotificationListsResponse
import com.milelog.retrofit.response.SignInResponse
import com.milelog.retrofit.response.TermsAgreeStatusResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.AlarmEntity
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.CheckForceUpdateState
import com.milelog.viewmodel.state.GetDriveHistoryMoreState
import com.milelog.viewmodel.state.GetMyCarInfoState
import com.milelog.viewmodel.state.GetTermsAgreeState
import com.milelog.viewmodel.state.PostReissueState

import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type


class SplashViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _checkForceUpdate = MutableLiveData<Event<CheckForceUpdateState>>()
    val checkForceUpdate: MutableLiveData<Event<CheckForceUpdateState>> get() = _checkForceUpdate

    private val _getMyCarInfo = MutableLiveData<Event<GetMyCarInfoState>>()
    val getMyCarInfo: MutableLiveData<Event<GetMyCarInfoState>> get() = _getMyCarInfo

    private val _getTermsAgree = MutableLiveData<Event<GetTermsAgreeState>>()
    val getTermsAgree: MutableLiveData<Event<GetTermsAgreeState>> get() = _getTermsAgree

    private val _postReissue = MutableLiveData<Event<PostReissueState>>()
    val postReissue: MutableLiveData<Event<PostReissueState>> get() = _postReissue

    fun init(context:Context){
        this.context = context

        viewModelScope.launch {

        }
    }

    fun postReissue(){
        apiService(context).postReissue(PreferenceUtil.getPref(context, PreferenceUtil.REFRESH_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val signInResponse = Gson().fromJson(response.body()?.string(), SignInResponse::class.java)

                    PreferenceUtil.putPref(context, PreferenceUtil.ACCESS_TOKEN, signInResponse.access_token)
                    PreferenceUtil.putPref(context, PreferenceUtil.REFRESH_TOKEN, signInResponse.refresh_token)
                    PreferenceUtil.putPref(context, PreferenceUtil.EXPIRES_IN, signInResponse.expires_in)
                    PreferenceUtil.putPref(context, PreferenceUtil.REFRESH_EXPIRES_IN, signInResponse.refresh_expires_in)
                    PreferenceUtil.putPref(context, PreferenceUtil.TOKEN_TYPE, signInResponse.token_type)

                    _postReissue.value = Event(PostReissueState.Success(signInResponse))

                }else{
                    _postReissue.value = Event(PostReissueState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                _postReissue.value = Event(PostReissueState.Empty)
            }

        })
    }

    fun getTermsAgree(){
        apiService(context).getTermsAgree("Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, ""), "MILELOG_USAGE").enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val type: Type = object : TypeToken<List<TermsAgreeStatusResponse?>?>() {}.type
                    val termsAgreeStatusResponses:List<TermsAgreeStatusResponse> = Gson().fromJson(jsonString, type)

                    _getTermsAgree.value = Event(GetTermsAgreeState.Success(termsAgreeStatusResponses))
                } else{
                    _getMyCarInfo.value = Event(GetMyCarInfoState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                _getMyCarInfo.value = Event(GetMyCarInfoState.Empty)
            }

        })
    }

    fun getMyCarInfo(){
        apiService(context).getMyCarInfo("Bearer " + PreferenceUtil.getPref(context, PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                    val getMyCarInfoResponse:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

                    _getMyCarInfo.value = Event(GetMyCarInfoState.Success(getMyCarInfoResponse))
                }else{
                    _getMyCarInfo.value = Event(GetMyCarInfoState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                _getMyCarInfo.value = Event(GetMyCarInfoState.Empty)
            }
        })
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