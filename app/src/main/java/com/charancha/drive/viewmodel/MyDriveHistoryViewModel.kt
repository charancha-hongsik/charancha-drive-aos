package com.charancha.drive.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.room.entity.DriveForApp
import kotlinx.coroutines.launch

class MyDriveHistoryViewModel: ViewModel() {
    lateinit var context: Context

    private val _setAllDriveDateForApp = MutableLiveData<Event<MutableList<DriveForApp>>>()
    val setAllDriveDateForApp: MutableLiveData<Event<MutableList<DriveForApp>>> get() = _setAllDriveDateForApp

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDao().allDriveForApp?.let {
                setAllDriveDateForApp.value = Event(it.toMutableList())
            }

//            driveDatabase.driveForApiDao().allDrive?.let {
//                Log.d("testsetsetsetset","testsetsetsetse1233 drive size:: " +it.size)
//
//                for(drive in it){
//                    Log.d("testsetsetsetset","testsetsetsetse1233 tracking_id:: " + drive.tracking_id)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 deviceModel:: " + drive.automobile)
//                }
//            }
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