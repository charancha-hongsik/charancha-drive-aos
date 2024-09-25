package com.milelog.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApp
import kotlinx.coroutines.launch

class MyDriveHistoryViewModel: BaseViewModel() {
    lateinit var context: Context

    private val _setAllDriveDateForApp = MutableLiveData<Event<MutableList<DriveForApp>>>()
    val setAllDriveDateForApp: MutableLiveData<Event<MutableList<DriveForApp>>> get() = _setAllDriveDateForApp

    fun init(context:Context){
        this.context = context
    }

    // 99b42990-d74f-440c-9cf1-2b3bb1cb6824
    // 1721339056037
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveForAppDao().allDriveForApp?.let {
                for(drive in it){
                    Log.d("testestests","testestestse ::" + drive.tracking_id)
                }
            }
        }
    }
}