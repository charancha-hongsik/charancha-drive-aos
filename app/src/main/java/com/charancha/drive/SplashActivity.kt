package com.charancha.drive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d("testsetestestest","testestsetestset :: " + PreferenceUtil.getBooleanPref(this, PreferenceUtil.HAVE_BEEN_HOME, false))

        if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.HAVE_BEEN_HOME, false)){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this, BetaInfoActivity::class.java))
            finish()
        }
    }
}