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

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        if(allPermissionsGranted()){
            setBtn()
            checkDeeplink()
        } else{

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
                if(!isMyServiceRunning(BluetoothService::class.java)){
                    val intent = Intent(this, BluetoothService::class.java)
                    startForegroundService(intent)
                }
            }
        } else{
            if(!isMyServiceRunning(BluetoothService::class.java)){
                val intent = Intent(this, BluetoothService::class.java)
                startForegroundService(intent)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        btnStop = findViewById(R.id.btn_stop)
        btnStop.setText("센서 종료")

        et_seconds = findViewById(R.id.et_seconds)

        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, MyDriveHistoryActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
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