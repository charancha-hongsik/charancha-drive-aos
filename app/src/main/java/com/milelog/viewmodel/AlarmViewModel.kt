package com.milelog.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.GetNotificationListsResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.AlarmEntity

import kotlinx.coroutines.launch


class AlarmViewModel: BaseViewModel() {
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
}