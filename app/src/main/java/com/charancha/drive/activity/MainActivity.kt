package com.charancha.drive.activity

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
    lateinit var btnStart:Button
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
                btnStart.performClick()
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
        btnStart = findViewById(R.id.btn_start)
        btnStart.setText("센서 시작")

        btnStart.setOnClickListener{
            if(btnStatus){
                stopService(Intent(this, SensorService::class.java))
                btnStart.setText("센서 시작")
                btnStatus = !btnStatus
            } else {
                var intent = Intent(this, SensorService::class.java)
                if(et_seconds.text.isNotEmpty())  intent.putExtra("interval",(et_seconds.text.toString().toLong()*1000))

                startForegroundService(intent)
                btnStart.setText("센서 멈춤")
                btnStatus = !btnStatus
            }
        }

        et_seconds = findViewById(R.id.et_seconds)

        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, MyDriveHistoryAvtivity::class.java))
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
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
                    ACCESS_COARSE_LOCATION,
                ).apply {

                }.toTypedArray()
            }
    }
}