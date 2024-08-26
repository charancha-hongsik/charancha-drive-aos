package com.milelog.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import com.milelog.retrofit.response.GetDrivingGraphDataResponse
import com.milelog.retrofit.response.GetNotificationListsResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.AlarmEntity
import com.milelog.room.entity.DriveForApp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AlarmViewModel: ViewModel() {
    lateinit var context: Context

    private val _setAllAlarm = MutableLiveData<Event<List<AlarmEntity>?>>()
    val setAllAlarm: MutableLiveData<Event<List<AlarmEntity>?>> get() = _setAllAlarm

    private val _updateIsRequired = MutableLiveData<Event<Long>>()
    val updateIsRequired: MutableLiveData<Event<Long>> get() = _updateIsRequired

    private val _notificationLists = MutableLiveData<Event<List<GetNotificationListsResponse>?>>()
    val notificationLists: MutableLiveData<Event<List<GetNotificationListsResponse>?>> get() = _notificationLists

    var alarmCnt = 0

    fun init(context:Context){
        this.context = context

        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.alarmDao().getAlarmCount(PreferenceUtil.getPref(context, PreferenceUtil.USER_ID, "")!!).let {
                alarmCnt = it
            }
        }
    }

    fun getAlarms(offset:Int){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.alarmDao().getAlarmLimit30(PreferenceUtil.getPref(context, PreferenceUtil.USER_ID, "")!!,offset).let {
                it?.let{
                    setAllAlarm.value = Event(it)
                }
            }
        }
    }

    fun updateIsRequired(idx:Long){
        val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
        driveDatabase.alarmDao().updateIsRequired(idx, false).let{
            if(it > 0){
                updateIsRequired.value = Event(idx)
            }
        }
    }

    open class Event<out T>(private val content: T) {
        var hasBeenHandled = false
            private set

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }

    class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
        override fun onChanged(event: Event<T>?) {
            event?.getContentIfNotHandled()?.let { value ->
                onEventUnhandledContent(value)
            }
        }
    }
}