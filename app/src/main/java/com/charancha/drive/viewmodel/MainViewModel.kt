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

    fun getAllDistance(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            Log.d("testestsetest","testsetsetestsetse :: " + driveDatabase.driveDao().getAllDistanceArrays(1716446441000, 1716446461000).size)
        }

    }

//    fun getAllFloatList(driveDao: DriveDao): List<List<Float>> {
//        val gson = Gson()
//        val listType = object : TypeToken<List<Float>>() {}.type
//
//        // JSON 문자열 리스트를 가져옵니다.
//        val jsonStringList = driveDao.getAllDistanceArrays(1716446441000, 1716446461000)
//
//        // JSON 문자열 리스트를 List<List<Float>>로 변환합니다.
//        return jsonStringList.map { jsonString ->
//            gson.fromJson(jsonString, listType)
//        }
//    }



}