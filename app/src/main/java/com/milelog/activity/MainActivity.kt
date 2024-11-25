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
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.CommonUtil
import com.milelog.CustomDialog
import com.milelog.PreferenceUtil
import com.milelog.PreferenceUtil.HAVE_BEEN_HOME
import com.milelog.R
import com.milelog.room.entity.MyCarsEntity
import com.milelog.service.BluetoothService
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.MainViewModel
import com.milelog.viewmodel.MyScoreViewModel
import com.milelog.viewmodel.state.MyCarInfoState
import com.milelog.viewmodel.state.NotSavedDataState

class MainActivity:BaseActivity() {
    private val mainViewModel: MainViewModel by viewModels()

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

        mainViewModel.getMyCarInfo()
        /**
         * 사용자에게 위치권한을 받은 후 앱으로 돌아왔을 때에 대한 동작
         */
        setNextPermissionProcess()
        setBluetoothService()
        mainViewModel.postDrivingInfoNotSavedData()
    }

    private fun setObserver(){
        mainViewModel.notSavedDataStateResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is NotSavedDataState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
            }
        })

        mainViewModel.myCarInfoResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is MyCarInfoState.Loading -> {

                }
                is MyCarInfoState.Success -> {
                    val getMyCarInfoResponses = state.data

                    val myCarsListOnServer: MutableList<MyCarsEntity> = mutableListOf()
                    val myCarsListOnDevice:MutableList<MyCarsEntity> = mutableListOf()

                    PreferenceUtil.getPref(this@MainActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                        if(it != "") {
                            val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                            myCarsListOnDevice.addAll(GsonBuilder().serializeNulls().create().fromJson(it, type))
                        }
                    }

                    if(getMyCarInfoResponses.items.size > 0){
                        for(car in getMyCarInfoResponses.items){
                            myCarsListOnServer.add(MyCarsEntity(car.id, car.carName, car.licensePlateNumber, null,null, type = car.type))
                        }

                        PreferenceUtil.putPref(this@MainActivity, PreferenceUtil.MY_CAR_ENTITIES, GsonBuilder().serializeNulls().create().toJson(updateMyCarList(myCarsListOnServer, myCarsListOnDevice)))

                    }else{
                        startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                        finish()
                    }
                }
                is MyCarInfoState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is MyCarInfoState.Empty -> {

                }
            }
        })

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

    fun updateMyCarList(
        myCarsListOnServer: MutableList<MyCarsEntity>,
        myCarsListOnDevice: MutableList<MyCarsEntity>
    ): MutableList<MyCarsEntity> {
        // 1. 유지할 리스트: 서버에 있는 차량만 남기고 type과 name 동기화
        val retainedCars = myCarsListOnDevice.mapNotNull { deviceCar ->
            myCarsListOnServer.find { serverCar -> serverCar.id == deviceCar.id }?.let { serverCar ->
                deviceCar.copy(name = serverCar.name, type = serverCar.type, isActive = serverCar.isActive) // type과 name을 서버의 값으로 동기화
            }
        }.toMutableList()

        // 2. 추가할 차량: 서버에 있는데 장치에 없는 차량 추가
        val newCarsToAdd = myCarsListOnServer.filterNot { serverCar ->
            myCarsListOnDevice.any { deviceCar -> deviceCar.id == serverCar.id }
        }

        // 3. 새 차량을 유지된 차량 리스트에 추가
        retainedCars.addAll(newCarsToAdd)

        // 업데이트된 리스트 반환
        return retainedCars
    }

}