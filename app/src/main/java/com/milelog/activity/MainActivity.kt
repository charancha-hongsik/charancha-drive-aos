package com.milelog.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.milelog.CommonUtil
import com.milelog.CustomDialog
import com.milelog.PreferenceUtil
import com.milelog.PreferenceUtil.HAVE_BEEN_HOME
import com.milelog.R
import com.milelog.service.BluetoothService

class MainActivity:BaseActivity() {
    lateinit var wv_main:WebView

    /**
     * for permission
     */
    var checkingUserActivityPermission = false
    var checkingIgnoreBatteryPermission = false

    /**
     * firebase
     */
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        setResources()
    }

    override fun onResume() {
        super.onResume()

        /**
         * 사용자에게 위치권한을 받은 후 앱으로 돌아왔을 때에 대한 동작
         */
        setNextPermissionProcess()
        setBluetoothService()
    }

    private fun init(){
        wv_main = findViewById(R.id.wv_main)

        if(!PreferenceUtil.getBooleanPref(this, HAVE_BEEN_HOME, false)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission(mutableListOf(
                    BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray(),0)
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocation()
            } else{
                if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                    checkUserActivity()
                }else{
                    setIgnoreBattery()
                }
            }
        }

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)
    }

    private fun setResources(){
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun checkLocation(){
        CustomDialog(this, "위치 정보 권한", "위치 서비스를 사용할 수 없습니다. 기기의 ‘마일로그 > 권한 > 위치”에서 위치 서비스를 “항상 허용\"으로 켜주세요 (필수 권한)", "설정으로 이동","취소",  object : CustomDialog.DialogCallback{
            override fun onConfirm() {
                val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                checkingUserActivityPermission = true
                startActivity(openSettingsIntent)
            }

            override fun onCancel() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if(ContextCompat.checkSelfPermission(this@MainActivity, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                        checkUserActivity()
                    }else{
                        setIgnoreBattery()
                    }
                }
            }

        }).show()
    }

    private fun checkUserActivity(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                CustomDialog(
                    this,
                    "신체 활동",
                    "신체 활동 서비스를 사용할 수 없습니다. 기기의 ‘마일로그 > 권한 > 신체 활동”에서 신체 활동을 “허용\" 으로 켜주세요 (필수 권한)",
                    "설정으로 이동",
                    "취소",
                    object : CustomDialog.DialogCallback {
                        override fun onConfirm() {
                            val openSettingsIntent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    val uri: Uri = Uri.fromParts("package", packageName, null)
                                    data = uri
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            checkingIgnoreBatteryPermission = true
                            startActivity(openSettingsIntent)
                        }

                        override fun onCancel() {
                            setIgnoreBattery()
                        }

                    }).show()
            }
        }
    }

    private fun setIgnoreBattery(){
        val i = Intent()
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        if(!pm.isIgnoringBatteryOptimizations(packageName)) {
            i.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            i.data = Uri.parse("package:$packageName")

            startActivity(i)
        }

        checkingIgnoreBatteryPermission = false
    }

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }

    private fun setNextPermissionProcess(){
        if(checkingUserActivityPermission){
            if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                checkingIgnoreBatteryPermission = true
                checkUserActivity()
            } else{
                setIgnoreBattery()
            }

            checkingUserActivityPermission = false
        }

        if(checkingIgnoreBatteryPermission){
            setIgnoreBattery()
        }
    }

    private fun setBluetoothService(){
        if(CommonUtil.checkRequiredPermissions(this@MainActivity)){
            val bluetoothIntent = Intent(this, BluetoothService::class.java)
            startForegroundService(bluetoothIntent)
        }else{
            if(isMyServiceRunning(BluetoothService::class.java)){
                val bluetoothIntent = Intent(this, BluetoothService::class.java)
                stopService(bluetoothIntent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            0 -> {
                for(permission in permissions){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        checkPermission(mutableListOf(
                            POST_NOTIFICATIONS
                        ).apply {

                        }.toTypedArray(),1)
                    }else{
                        setIgnoreBattery()
                    }
                }
            }

            1->{
                setIgnoreBattery()
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}