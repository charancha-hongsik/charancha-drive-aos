package com.milelog.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import android.view.View.VISIBLE
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.BuildConfig.BASE_API_URL
import com.milelog.CommonUtil
import com.milelog.CustomDialog
import com.milelog.Endpoints.HOME
import com.milelog.PreferenceUtil
import com.milelog.PreferenceUtil.HAVE_BEEN_HOME
import com.milelog.R
import com.milelog.room.entity.MyCarsEntity
import com.milelog.service.BluetoothService
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.MainViewModel
import com.milelog.viewmodel.state.MyCarInfoState
import com.milelog.viewmodel.state.NotSavedDataState
import java.io.File
import java.io.FileOutputStream

/**
 * 파이어베이스 설정
 * 퍼미션
 * 딥링크
 * 주행데이터 전송
 * MyCarInfo 업데이트
 * HAVE_BEEN_HOME 처리
 */
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
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val results: Array<Uri>?

                // 여러 개의 파일이 선택되었을 때 처리
                if (data?.clipData != null) {
                    val clipData = data.clipData
                    val uris = ArrayList<Uri>()
                    for (i in 0 until clipData!!.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                    results = uris.toArray(arrayOfNulls(uris.size)) // 여러 파일 선택
                } else {
                    // 하나의 파일만 선택되었을 때 처리
                    val uri = data?.data
                    results = if (uri != null) arrayOf(uri) else emptyArray() // 하나의 파일 선택
                }

                // results가 비어있지 않으면 처리
                if (results.isNullOrEmpty()) {
                    fileChooserCallback?.onReceiveValue(null)
                } else {
                    // 선택된 파일들을 모두 처리하기 위해 리스트로 저장
                    val compressedUris = mutableListOf<Uri>()

                    // 모든 선택된 파일에 대해 처리
                    for (selectedUri in results) {
                        // 선택된 파일을 압축하여 40% 퀄리티로 새로운 파일로 저장
                        val compressedFile = compressImage(selectedUri, this)

                        if (compressedFile != null) {
                            Log.d("test", "Compressed file exists")
                            // 압축된 파일을 Uri로 변환
                            val compressedUri = Uri.fromFile(compressedFile)
                            compressedUris.add(compressedUri)
                        } else {
                            Log.d("test", "Compressed file is null")
                        }
                    }

                    // 압축된 파일들이 있다면 콜백으로 전달
                    if (compressedUris.isNotEmpty()) {
                        fileChooserCallback?.onReceiveValue(compressedUris.toTypedArray())
                    } else {
                        fileChooserCallback?.onReceiveValue(null)
                    }
                }
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }

            fileChooserCallback = null
        }
    // 이미지를 압축하여 40% 퀄리티로 저장하는 함수
    fun compressImage(uri: Uri, context: Context): File? {
        try {
            // 선택된 Uri에서 이미지 경로를 얻고, Bitmap으로 변환
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            // 압축된 파일 경로
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // Bitmap을 압축하여 파일로 저장 (70% 품질)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()

            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        setObserver()
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
        mainViewModel.init(applicationContext)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    fun setWebview(){
        wv_main = findViewById(R.id.wv_main)
        wv_main.visibility = VISIBLE
        wv_main.settings.loadWithOverviewMode = true // 화면에 맞게 WebView 사이즈를 정의
        wv_main.settings.useWideViewPort = true //html 컨텐츠가 웹뷰에 맞게 나타나도록 합니다.
        wv_main.settings.defaultTextEncodingName = "UTF-8" // TextEncoding 이름 정의
        wv_main.settings.javaScriptEnabled = true
        wv_main.settings.userAgentString = "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
        wv_main.settings.domStorageEnabled = true
        wv_main.settings.cacheMode = WebSettings.LOAD_DEFAULT
        wv_main.settings.textZoom = 100 // System 텍스트 사이즈 변경되지 않게
        wv_main.settings.allowContentAccess = true
        wv_main.settings.allowFileAccess = true

        //chrome inspect 디버깅 모드
        WebView.setWebContentsDebuggingEnabled(true)

        // javascriptInterface 설정
        wv_main.addJavascriptInterface(MilelogPublicApi(this), "MilelogPublicApi")

        wv_main.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback = filePathCallback

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"

                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))


                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    fileChooserCallback = null
                    return false
                }

                return true
            }

        }

        wv_main.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                syncCookie()
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                Log.d("", "request.getRequestHeaders()::"+request?.getRequestHeaders());
                return super.shouldInterceptRequest(view, request)
            }
        }

        val headers = mapOf("Authorization" to "Bearer " + PreferenceUtil.getPref(this,  PreferenceUtil.ACCESS_TOKEN, "")!!)
        wv_main.loadUrl(BASE_API_URL + HOME , headers)

        // 쿠키 설정
        syncCookie()
    }


    private fun setResources(){
        checkPermission()
        checkDeeplink()
        setWebview()
    }

    private fun syncCookie(){
        wv_main.settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv_main, true)
        cookieManager.flush()
    }

    private fun checkPermission(){
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

    private fun checkDeeplink(){
        if(intent.getBooleanExtra("deeplink",false)){
            startActivity(Intent(this@MainActivity, AlarmActivity::class.java))
        }
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

    class MilelogPublicApi(val activity: MainActivity) {
        @JavascriptInterface
        fun openMyPage(){
            activity.startActivity(Intent(activity, MyPageActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openMyPage ")
        }

        @JavascriptInterface
        fun openNotification(){
            activity.startActivity(Intent(activity, AlarmActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openNotification ")

        }

        @JavascriptInterface
        fun openMyGarage(){
            activity.startActivity(Intent(activity, MyGarageActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openMyGarage ")

        }

        @JavascriptInterface
        fun openDrivingDetail(trackingId:String){
            activity.startActivity(Intent(activity, DetailDriveHistoryActivity::class.java).putExtra("trackingId", trackingId))
            Log.d("testsetestestset","testestestestset ::openDrivingDetail ")

        }

        @JavascriptInterface
        fun openDrivings(){
            activity.startActivity(Intent(activity, MyDriveHistoryActivity::class.java))
            Log.d("testsetestestset","testestestestset ::openDrivings ")

        }

        @JavascriptInterface
        fun openDrivingDistanceStats(userCarId: String){
            activity.startActivity(Intent(activity, DrivenDistanceActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingDistanceStats ")

        }

        @JavascriptInterface
        fun openAverageDrivingDistanceStats(userCarId: String){
            activity.startActivity(Intent(activity, AverageDrivenDistanceActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openAverageDrivingDistanceStats ")

        }

        @JavascriptInterface
        fun openDrivingTimeStats(userCarId: String){
            activity.startActivity(Intent(activity, DrivenTimeActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingTimeStats ")

        }


        @JavascriptInterface
        fun openDrivingScoreStats(userCarId: String){
            activity.startActivity(Intent(activity, MyScoreActivity::class.java).putExtra("userCarId", userCarId))
            Log.d("testsetestestset","testestestestset ::openDrivingScoreStats :: " + userCarId)
        }

        @JavascriptInterface
        fun getAccessToken():String{
            return PreferenceUtil.getPref(activity, PreferenceUtil.ACCESS_TOKEN, "")!!
        }

        @JavascriptInterface
        fun getRefreshToken():String{
            return PreferenceUtil.getPref(activity, PreferenceUtil.REFRESH_TOKEN, "")!!
        }

        @JavascriptInterface
        fun closeWebview(){
            activity.finish()
        }

        @JavascriptInterface
        fun openBrowser(url:String){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")
            try {
                activity.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // Chrome browser presumably not installed so allow user to choose instead
                intent.setPackage(null)
                activity.startActivity(intent)
            }
        }
    }

}