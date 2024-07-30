package com.milelog

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        WorkManager.initialize(this, Configuration.Builder().build())

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
    }
}