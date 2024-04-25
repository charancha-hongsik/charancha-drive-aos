package com.charancha.drive

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
                Log.d("testestststest","testsetsetsetsets size :: " + it.size)
            }
        }
    }

}