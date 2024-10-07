package com.milelog.viewmodel

import android.content.Context
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.PatchDrivingInfo
import com.milelog.retrofit.response.GetAccountResponse
import com.milelog.retrofit.response.PatchDrivingResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApp
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.PatchDrivingInfoState
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailDriveHistoryViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _setAllDriveDateForApp = MutableLiveData<Event<MutableList<DriveForApp>>>()
    val setAllDriveDateForApp: MutableLiveData<Event<MutableList<DriveForApp>>> get() = _setAllDriveDateForApp

    private val _setDriveForApp = MutableLiveData<Event<DriveForApp?>>()
    val setDriveForApp: MutableLiveData<Event<DriveForApp?>> get() = _setDriveForApp

    private val _patchDrivingInfo = MutableLiveData<Event<PatchDrivingInfoState>>()
    val patchDrivingInfo: MutableLiveData<Event<PatchDrivingInfoState>> get() = _patchDrivingInfo

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveForAppDao().allDriveForApp?.let {
                setAllDriveDateForApp.value = Event(it.toMutableList())
            }
        }
    }

    fun getDrive(trackingId:String){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveForAppDao().getDriveByTrackingId(trackingId)?.let {
                setDriveForApp.value = Event(it)
            } ?: run{
                setDriveForApp.value = Event(null)
            }
        }
    }

    fun patchDrivingInfo(isActive:Boolean, tracking_id:String){
        val gson = Gson()
        val jsonParam =
            gson.toJson(PatchDrivingInfo(isActive))
        apiService(context).patchDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val patchDrivingResponse = Gson().fromJson(
                            response.body()?.string(),
                            PatchDrivingResponse::class.java
                        )
                        _patchDrivingInfo.value = Event(PatchDrivingInfoState.Success(patchDrivingResponse))
                    }else if(response.code() == 401){
                        _patchDrivingInfo.value = Event(PatchDrivingInfoState.Error(response.code(), response.message()))
                    }
                }catch (e:Exception){
                    _patchDrivingInfo.value = Event(PatchDrivingInfoState.Empty)

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _patchDrivingInfo.value = Event(PatchDrivingInfoState.Empty)
            }

        })
    }
}