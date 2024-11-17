package com.milelog.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.milelog.PreferenceUtil
import com.milelog.retrofit.request.PatchCorpType
import com.milelog.retrofit.request.PatchDrivingInfo
import com.milelog.retrofit.request.PatchMemo
import com.milelog.retrofit.response.GetDrivingInfoResponse
import com.milelog.retrofit.response.PatchCorpTypeResponse
import com.milelog.retrofit.response.PatchDrivingResponse
import com.milelog.retrofit.response.PatchMemoResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApp
import com.milelog.viewmodel.state.GetDrivingInfoState
import com.milelog.viewmodel.state.PatchCorpTypeState
import com.milelog.viewmodel.state.PatchDrivingInfoState
import com.milelog.viewmodel.state.PatchMemoState
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

    private val _getMapData = MutableLiveData<Event<DriveForApp?>>()
    val getMapData: MutableLiveData<Event<DriveForApp?>> get() = _getMapData

    private val _patchDrivingInfo = MutableLiveData<Event<PatchDrivingInfoState>>()
    val patchDrivingInfo: MutableLiveData<Event<PatchDrivingInfoState>> get() = _patchDrivingInfo

    private val _patchMemo = MutableLiveData<Event<PatchMemoState>>()
    val patchMemo: MutableLiveData<Event<PatchMemoState>> get() = _patchMemo

    private val _patchCorpType = MutableLiveData<Event<PatchCorpTypeState>>()
    val patchCorpType: MutableLiveData<Event<PatchCorpTypeState>> get() = _patchCorpType

    private val _getDrivingInfo = MutableLiveData<Event<GetDrivingInfoState>>()
    val getDrivingInfo: MutableLiveData<Event<GetDrivingInfoState>> get() = _getDrivingInfo

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

    fun getMapData(trackingId:String){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveForAppDao().getDriveByTrackingId(trackingId)?.let {
                _getMapData.value = Event(it)
            } ?: run{
                _getMapData.value = Event(null)
            }
        }
    }

    fun patchDrivingInfo(isActive:Boolean,userCarId:String?,tracking_id:String){
        val gson = GsonBuilder().serializeNulls().create()
        val jsonParam =
            gson.toJson(PatchDrivingInfo(userCarId, isActive))
        apiService(context).patchDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val patchDrivingResponse = GsonBuilder().serializeNulls().create().fromJson(
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

    fun patchMemo(memo:String, tracking_id:String){
        val gson = GsonBuilder().serializeNulls().create()
        val jsonParam =
            gson.toJson(PatchMemo(memo))
        apiService(context).patchDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val patchMemoResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            PatchMemoResponse::class.java
                        )
                        _patchMemo.value = Event(PatchMemoState.Success(patchMemoResponse))
                    }else if(response.code() == 401){
                        _patchMemo.value = Event(PatchMemoState.Error(response.code(), response.message()))
                    }
                }catch (e:Exception){
                    _patchMemo.value = Event(PatchMemoState.Empty)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _patchMemo.value = Event(PatchMemoState.Empty)
            }

        })
    }

    fun patchCorpType(type:String, tracking_id:String){
        val gson = GsonBuilder().serializeNulls().create()
        val jsonParam =
            gson.toJson(PatchCorpType(type))
        apiService(context).patchDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val patchCorpTypeResponse = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            PatchCorpTypeResponse::class.java
                        )
                        _patchCorpType.value = Event(PatchCorpTypeState.Success(patchCorpTypeResponse))
                    }else if(response.code() == 401){
                        _patchCorpType.value = Event(PatchCorpTypeState.Error(response.code(), response.message()))
                    }
                }catch (e:Exception){
                    _patchCorpType.value = Event(PatchCorpTypeState.Empty)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _patchCorpType.value = Event(PatchCorpTypeState.Empty)
            }

        })
    }


    fun getDrivingInfo(tracking_id:String){
        apiService(context).getDrivingInfo("Bearer " + PreferenceUtil.getPref(context,  PreferenceUtil.ACCESS_TOKEN, "")!!, tracking_id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()
                    val getDrivingInfoResponse = GsonBuilder().serializeNulls().create().fromJson(jsonString, GetDrivingInfoResponse::class.java)
                    _getDrivingInfo.value = Event(GetDrivingInfoState.Success(getDrivingInfoResponse))
                }else if(response.code() == 401){
                    _getDrivingInfo.value = Event(GetDrivingInfoState.Error(response.code(), response.message()))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}