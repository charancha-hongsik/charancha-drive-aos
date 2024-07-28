package com.milelog

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        WorkManager.initialize(this, Configuration.Builder().build())
    }
}