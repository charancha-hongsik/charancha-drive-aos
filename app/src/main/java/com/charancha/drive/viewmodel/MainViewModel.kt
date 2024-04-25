package com.charancha.drive.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charancha.drive.room.database.DriveDatabase
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    lateinit var context: Context

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDao().allDrive?.let {
                for(drive in it)
                    Log.d("testestestest","testsetsetset drive :: " + drive.toString())
            }
        }
    }

    fun getAllDriveDate(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDateDao().allDriveDate?.let {
                for(driveDate in it)
                    Log.d("testestestest","testsetsetset driveDate :: " + driveDate.toString())

            }
        }
    }

}