package com.charancha.drive.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.room.entity.Drive
import kotlinx.coroutines.launch

class MyDriveHistoryViewModel: ViewModel() {
    lateinit var context: Context

    private val _setAllDriveDate = MutableLiveData<Event<MutableList<Drive>>>()
    val setAllDriveDate: MutableLiveData<Event<MutableList<Drive>>> get() = _setAllDriveDate

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDao().allDrive?.let {
                setAllDriveDate.value = Event(it.toMutableList())
            }

            driveDatabase.driveForApiDao().allDrive?.let {
                Log.d("testsetsetsetset","testsetsetsetse1233 drive size:: " +it.size)

//                for(drive in it){
//                    Log.d("testsetsetsetset","testsetsetsetse1233 tracking_id:: " + drive.tracking_id)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 deviceModel:: " + drive.deviceModel)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 deviceUuid:: " + drive.deviceUuid)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 verification:: " + drive.verification)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 username:: " + drive.username)
//                    Log.d("testsetsetsetset","testsetsetsetse1233 endTimestamp:: " + drive.endTimestamp)
//
//                    for(data in drive.gpses){
//                        Log.d("testsetsetsetset", "testsetsetsetse1233 drive:: $data")
//
//                    }
//                }
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