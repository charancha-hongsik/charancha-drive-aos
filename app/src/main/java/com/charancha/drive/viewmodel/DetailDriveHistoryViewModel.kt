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

class DetailDriveHistoryViewModel: ViewModel() {
    lateinit var context: Context

    private val _setAllDriveDate = MutableLiveData<Event<MutableList<Drive>>>()
    val setAllDriveDate: MutableLiveData<Event<MutableList<Drive>>> get() = _setAllDriveDate

    private val _setDrive = MutableLiveData<Event<Drive>>()
    val setDrive: MutableLiveData<Event<Drive>> get() = _setDrive

    fun init(context:Context){
        this.context = context
    }
    fun getAllDrive(){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDao().allDrive?.let {
                setAllDriveDate.value = Event(it.toMutableList())
            }
        }
    }

    fun getDrive(trackingId:String){
        viewModelScope.launch {
            val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(context)
            driveDatabase.driveDao().getDriveByTrackingId(trackingId)?.let {
                setDrive.value = Event(it)
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