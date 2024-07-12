package com.charancha.drive.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charancha.drive.room.dao.DriveForAppDao
import com.charancha.drive.room.database.DriveDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    lateinit var context: Context

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveForApiDao().allDrive?.let {
                for(drive in it)
                    Log.d("testestestest","testsetsetset drive :: " + drive.toString())
            }
        }
    }

}