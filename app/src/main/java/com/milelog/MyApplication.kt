package com.milelog

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.FirebaseApp
import java.io.File


class MyApplication : Application() {
    companion object {
        var isInForeground: Boolean = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
//        WorkManager.initialize(this, Configuration.Builder().build())

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                isInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                isInForeground = false
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}