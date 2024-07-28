package com.milelog.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milelog.room.database.DriveDatabase
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

            }
        }
    }

}