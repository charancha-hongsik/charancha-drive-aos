package com.milelog.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApp
import kotlinx.coroutines.launch

class DetailDriveHistoryViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _setAllDriveDateForApp = MutableLiveData<Event<MutableList<DriveForApp>>>()
    val setAllDriveDateForApp: MutableLiveData<Event<MutableList<DriveForApp>>> get() = _setAllDriveDateForApp

    private val _setDriveForApp = MutableLiveData<Event<DriveForApp?>>()
    val setDriveForApp: MutableLiveData<Event<DriveForApp?>> get() = _setDriveForApp

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
}