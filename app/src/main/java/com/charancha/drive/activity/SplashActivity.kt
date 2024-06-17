package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.HAVE_BEEN_HOME, false)){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, BetaInfoActivity::class.java))
                finish()
            }
        }, 2000) // 2000 밀리초 (2초)
    }
}