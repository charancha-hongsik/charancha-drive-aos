package com.charancha.drive.activity

import android.Manifest.permission.*
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.View.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.PreferenceUtil.HAVE_BEEN_HOME
import com.charancha.drive.R
import com.charancha.drive.service.BluetoothService
import com.charancha.drive.viewmodel.MainViewModel
import java.util.*


/**
 * 홈화면에 필요한 데이터
 * 1. 평균 주행 거리 (지난 30일간)
 * 2. 평균 주행 시간 (지난 30일간)
 * 3. 최근 관리 점수
 * 4. 평균 점수
 * 5. 최근 주행 총점
 * 6.
 */
class MainActivity : AppCompatActivity() {
    lateinit var btnHistory: ImageButton
    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        if(allPermissionsGranted()){
            setBtn()
        } else{

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, MyDriveHistoryActivity::class.java))
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    companion object {
        private val REQUIRED_PERMISSIONS =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                mutableListOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION,
                    BLUETOOTH_CONNECT,
                    POST_NOTIFICATIONS
                ).apply {

                }.toTypedArray()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mutableListOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION,
                    BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray()
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ).apply {

                }.toTypedArray()
            }
    }

}