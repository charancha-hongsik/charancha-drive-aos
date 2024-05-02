package com.charancha.drive.activity

import android.Manifest.permission.*
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.PreferenceUtil.HAVE_BEEN_HOME
import com.charancha.drive.R
import com.charancha.drive.service.BluetoothService
import com.charancha.drive.service.SensorService
import com.charancha.drive.viewmodel.MainViewModel
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var btnStop:Button
    lateinit var et_seconds:EditText
    lateinit var btnHistory:Button
    var btnStatus:Boolean = false
    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent()
//            val packageName = packageName
//            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:$packageName")
//                startActivity(intent)
//            }
//        }

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        if(allPermissionsGranted()){
            setBtn()
            checkDeeplink()
        } else{
            /**
             * 허용되지 않은 경우 -> 팝업 노출?
             */
        }
    }

    private fun checkDeeplink(){
        if(intent != null){
            if(intent.hasExtra("activityType")){
                /**
                 * startForegroundService TestService
                 */
                btnStop.performClick()
            } else{
                val intent = Intent(this, BluetoothService::class.java)
                startForegroundService(intent)
            }
        } else{
            val intent = Intent(this, BluetoothService::class.java)
            startForegroundService(intent)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        btnStop = findViewById(R.id.btn_stop)
        btnStop.setText("센서 종료")

        btnStop.setOnClickListener{
            stopService(Intent(this, SensorService::class.java))
            btnStop.visibility = GONE
        }

        et_seconds = findViewById(R.id.et_seconds)

        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, MyDriveHistoryActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()

        if(isMyServiceRunning(SensorService::class.java)){
            btnStop.visibility = VISIBLE
        } else{
            btnStop.visibility = GONE
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