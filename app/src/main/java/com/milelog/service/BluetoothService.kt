package com.milelog.service

import android.Manifest
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.app.*
import android.app.PendingIntent.*
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.database.Cursor
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.milelog.CommonUtil
import com.milelog.CommonUtil.apiService
import com.milelog.CommonUtil.getDateFromTimeStampToHH
import com.milelog.CommonUtil.getDateFromTimeStampToSS
import com.milelog.CommonUtil.isBluetoothDeviceConnected
import com.milelog.CommonUtil.isInternetConnected
import com.milelog.NotificationDeleteReceiver
import com.milelog.NotificationDeleteReceiver.Companion.ACTION_RESTART_NOTIFICATION
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.response.PostDrivingInfoResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.dto.EachGpsDtoForApi
import com.milelog.room.dto.EachGpsDtoForApp
import com.milelog.room.entity.DetectUserEntity
import com.milelog.room.entity.DriveForApi
import com.milelog.room.entity.DriveForApp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * The Service will only run in one instance. However, everytime you start the service, the onStartCommand() method is called.
 */
class BluetoothService : Service() {
    companion object {
        // columnName for provider to query on connection status
        const val CAR_CONNECTION_STATE = "CarConnectionState"

        const val TRANSITIONS_RECEIVER_ACTION = "TRANSITIONS_RECEIVER_ACTION"

        // auto app on your phone will send broadcast with this action when connection state changes
        const val ACTION_CAR_CONNECTION_UPDATED = "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED"

        // phone is not connected to car
        const val CONNECTION_TYPE_NOT_CONNECTED = 0

        const val L4 = "L4"
        const val L3 = "L3"
        const val L2 = "L2"
        const val L1 = "L1"

        private const val QUERY_TOKEN = 42

        private const val CAR_CONNECTION_AUTHORITY = "androidx.car.app.connection"

        private val PROJECTION_HOST_URI = Uri.Builder().scheme("content").authority(
            CAR_CONNECTION_AUTHORITY
        ).build()
    }

    private lateinit var carConnectionQueryHandler: CarConnectionQueryHandler

    val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(ACTION_CAR_CONNECTION_UPDATED)
    }

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ SensorService 관련 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    var distance_array = MutableList(24) { 0f } // 24개 시간대의 distance

    /**
     * 1. stopSensor 여러번 되지 않게
     * 2. API 호출되는 동안 startSensor 되지 않게
     */
    private var driveDatabase: DriveDatabase? = null

    private var fusedLocationClient :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    /**
     * 위치 센서
     */
    private var pastLocation: Location? = null
    private var pastSpeed: Float = 0f
    private var pastTimeStamp = 0L


    private var INTERVAL = 1000L

    /**
     * locationRequest.setInterval(INTERVAL) // INTERVAL 초마다 업데이트 요청
     * locationRequest.setFastestInterval(FASTEST_INTERVAL) 다른 앱에서 연산된 위치를 수신
     * setinterval() 메서드를 사용하여 앱을 위해 위치를 연산하는 간격을 지정합니다.
     * setFastestInterval()을 사용하여 다른 앱에서 연산된 위치를 수신하는 간격을 지정합니다.
     */

    val MS_TO_KH = 3.6f

    /**
     * room 데이터
     * 초기화 - startSensor 시
     */
    lateinit var driveForApp: DriveForApp
    lateinit var gpsInfoForApp: MutableList<EachGpsDtoForApp>

    /**
     * 저장 실패했을 때
     * 저장 용도의 변수
     * 초기화 - startSensor 시
     */
    lateinit var driveForApi: DriveForApi
    lateinit var gpsInfoForApi: MutableList<EachGpsDtoForApi>

    /**
     *  타이머 시간동안 최대 반경을 구하기 위한 변수들
     *  초기화 - startSensor 시
     */
    var thirtyMinCheckpointLocation: Location? = null
    var maxDistance = mutableListOf<Float>()
    var pastMaxDistance = mutableListOf<Float>()
    private var firstLocation: Location? = null

    /**
     * notification 관련
     */

    lateinit var notification: NotificationCompat.Builder
    val CHANNEL_ID = "my_channel_02"
    val channel = NotificationChannel(
        CHANNEL_ID,
        "상단바(필수)",
        NotificationManager.IMPORTANCE_DEFAULT
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /**
         * BootReceiver 에서 실행 시킨 경우 필수 권한 확인 후 Service 실행하기 위한 로직
         */
        if(!CommonUtil.checkRequiredPermissions(this@BluetoothService)){
            stopSelf()
            return START_STICKY
        }

        driveDatabase = DriveDatabase.getDatabase(this)
        carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

        if(fusedLocationClient == null){
            /**
             * TransitionsReceiver(L2, L3) 등록
             */
            registerDetectCarConnectedReceiver()

            /**
             * WalkingDetectReceiver(L1) 등록
             */
            registerDetectUserActivityReceiver()

            /**
             * Notification 띄우기
             */
            showNotification()

            /**
             * 사용자 활동 탐지 시작
             */
            scheduleWalkingDetectWork()
        }else{
            /**
             * 주행중인 경우 && 사용자가 임의로 notification을 없앤 경우
             */
            if(intent?.action == ACTION_RESTART_NOTIFICATION){
                showNotification()
            }
        }

        return START_STICKY
    }

    class DetectUserActivityReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)
                result?.let {
                    for (event in it.transitionEvents) {
                        val activityType = event.activityType
                        val transitionType = event.transitionType

                        if(activityType == DetectedActivity.WALKING) {
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                // Walking 활동에 들어감
                                (context as BluetoothService).driveDatabase?.detectUserDao()?.insert(
                                    DetectUserEntity(
                                        user_id = "",
                                        verification = "L1",
                                        start_stop = "Walking Enter(stop)",
                                        timestamp = System.currentTimeMillis().toString(),
                                        sensor_state = context.fusedLocationClient != null
                                    )
                                )
                                context.stopSensor()

                            }
                        } else if(activityType == DetectedActivity.IN_VEHICLE){
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                (context as BluetoothService).driveDatabase?.detectUserDao()?.insert(
                                    DetectUserEntity(
                                        user_id = "",
                                        verification = "L1",
                                        start_stop = "IN VEHICLE Enter(start)",
                                        timestamp = System.currentTimeMillis().toString(),
                                        sensor_state = context.fusedLocationClient != null
                                    )
                                )

                                context.startSensor(L1)
                            }
                        }
                    }
                }
            }
        }
    }

    inner class DetectCarConnectedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(ContextCompat.checkSelfPermission(this@BluetoothService, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                        pairedDevices?.forEach { device ->
                            if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE){
                                if(isBluetoothDeviceConnected(device)){
                                    driveDatabase?.detectUserDao()?.insert(
                                        DetectUserEntity(
                                            user_id = "",
                                            verification = "L2",
                                            start_stop = "AUDIO_VIDEO_HANDSFREE(start)",
                                            timestamp = System.currentTimeMillis().toString(),
                                            sensor_state = fusedLocationClient != null
                                        )
                                    )

                                    startSensor(L2)
                                }
                            }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                        pairedDevices?.forEach { device ->
                            if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE){
                                if(!isBluetoothDeviceConnected(device)){
                                    driveDatabase?.detectUserDao()?.insert(
                                        DetectUserEntity(
                                            user_id = "",
                                            verification = "L2",
                                            start_stop = "AUDIO_VIDEO_HANDSFREE(stop)",
                                            timestamp = System.currentTimeMillis().toString(),
                                            sensor_state = fusedLocationClient != null
                                        )
                                    )

                                    stopSensor(L2)
                                }
                            }
                        }
                    }
                    else -> {
                        queryForState()
                    }
                }
            }
        }
    }

    inner class CarConnectionQueryHandler(resolver: ContentResolver?) : AsyncQueryHandler(resolver) {

        // notify new queryed connection status when query complete
        override fun onQueryComplete(token: Int, cookie: Any?, response: Cursor?) {
            if (response == null) {
                return
            }
            val carConnectionTypeColumn = response.getColumnIndex(CAR_CONNECTION_STATE)
            if (carConnectionTypeColumn < 0) {
                return
            }
            if (!response.moveToNext()) {
                return
            }
            val connectionState = response.getInt(carConnectionTypeColumn)
            if (connectionState == CONNECTION_TYPE_NOT_CONNECTED) {
                driveDatabase?.detectUserDao()?.insert(
                    DetectUserEntity(
                        user_id = "",
                        verification = "L3",
                        start_stop = "CAR_CONNECTION_STATE(stop)",
                        timestamp = System.currentTimeMillis().toString(),
                        sensor_state = fusedLocationClient != null
                    )
                )
                stopSensor(L3)
            } else {
                driveDatabase?.detectUserDao()?.insert(
                    DetectUserEntity(
                        user_id = "",
                        verification = "L3",
                        start_stop = "CAR_CONNECTION_STATE(start)",
                        timestamp = System.currentTimeMillis().toString(),
                        sensor_state = fusedLocationClient != null
                    )
                )
                startSensor(L3)
            }
        }
    }

    private fun registerDetectCarConnectedReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(DetectCarConnectedReceiver(), filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(DetectCarConnectedReceiver(), filter)
        }
    }

    private fun registerDetectUserActivityReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Detecting L1 Receiver
            registerReceiver(DetectUserActivityReceiver(), IntentFilter().apply {
                addAction(TRANSITIONS_RECEIVER_ACTION)
            }, RECEIVER_EXPORTED)
        } else {
            // Detecting L1 Receiver
            registerReceiver(DetectUserActivityReceiver(), IntentFilter().apply {
                addAction(TRANSITIONS_RECEIVER_ACTION)
            })
        }
    }

    private fun showNotification(){
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        notification = NotificationCompat.Builder(this, CHANNEL_ID)

        val deleteIntent = Intent(this@BluetoothService, NotificationDeleteReceiver::class.java)
        val pendingDeleteIntent = getBroadcast(
            this@BluetoothService,
            0,
            deleteIntent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification
                .setSmallIcon(R.mipmap.ic_notification)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText("주행 관찰중이에요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setDeleteIntent(pendingDeleteIntent)
                .build(), FOREGROUND_SERVICE_TYPE_HEALTH)
        }else{
            startForeground(1, notification
                .setSmallIcon(R.mipmap.ic_notification)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText("주행 관찰중이에요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build())
        }
    }

    private fun initDriveData(level:String){
        var startTimeStamp = System.currentTimeMillis()
        initDriveForApp(startTimeStamp)
        initDriveForApi(level,startTimeStamp)

        firstLocation = null
        thirtyMinCheckpointLocation = null
        maxDistance = mutableListOf()
        pastMaxDistance = mutableListOf()
        distance_array = MutableList(24) { 0f } // 23개 시간대의 distance
        PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
    }

    private fun initDriveForApp(startTimeStamp:Long){
        gpsInfoForApp = mutableListOf()
        driveForApp = DriveForApp(
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!! + startTimeStamp,
            gpsInfoForApp)
    }

    private fun initDriveForApi(level:String, startTimeStamp:Long){
        gpsInfoForApi = mutableListOf()
        driveForApi = DriveForApi(
            tracking_id = PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!! + startTimeStamp,
            userCarId = PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            startTimestamp = startTimeStamp,
            endTimestamp = 0L,
            verification = level,
            gpses = gpsInfoForApi,
        )
    }

    /**
     * sensor가 이미 켜져있으면 켜지지않음.
     */
    fun startSensor(level:String){
        try {
            if (fusedLocationClient == null) {
                initDriveData(level)
                setLocation()
            }
        } catch(e:Exception){
            stopSensorNotForSaving()
        }
    }

    /**
     * sensor 상태가 On일 때 끌 수 있음.
     * level이 같아야 끌 수 있음.
     */
    private fun stopSensor(level:String){
        try {
            if (fusedLocationClient != null) {
                if (level == PreferenceUtil.getPref(this, PreferenceUtil.RUNNING_LEVEL, "")) {
                    if(distance_array.sum() > 500f){
                        callApi(driveForApi.copy(gpses = driveForApi.gpses.map{it.copy()}), driveForApp.copy(gpses = driveForApp.gpses.map{it.copy()}))
                    }
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                    fusedLocationClient = null
                }
            }
        }catch (e:Exception){
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            fusedLocationClient = null
        }
    }

    fun stopSensor(){
        try {
            if (fusedLocationClient != null) {
                if(distance_array.sum() > 500f){
                    callApi(driveForApi.copy(gpses = driveForApi.gpses.map{it.copy()}), driveForApp.copy(gpses = driveForApp.gpses.map{it.copy()}))
                }
                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null

            }
        }catch(e:Exception){
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            fusedLocationClient = null
        }
    }

    fun stopSensorNotForSaving(){
        try {
            if (fusedLocationClient != null) {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null
            }
        }catch(e:Exception){
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            fusedLocationClient = null
        }
    }

    /**
     * PRIORITY_BALANCED_POWER_ACCURACY 도시 블록 내의 위치 정밀도 요청. 정확도는 대략 100미터. Wi-Fi 정보와 휴대폰 기지국 위치를 사용할 수 있음. 대략적인 수준의 정확성으로 전력을 비교적 적게 사용함.
     * PRIORITY_HIGH_ACCURACY 가장 정확한 위치를 요청. 이 설정을 사용하면 위치 서비스가 GPS를 사용하여 위치를 확인할 가능성이 높음.
     * PRIORITY_LOW_POWER 도시 수준의 정밀도 요청. 대략 10킬로미터의 정확성. 아주 대략적인 수준의 정확성으로 전력을 더 적게 소비함.
     * PRIORITY_NO_POWER 전력 소비에 별다른 영향을 미치지 않으면서 사용 가능한 경우 위치 업데이트를 수신하려면 이 설정을 사용. 해당 설정을 사용할 경우 앱에서 위치를 트리거하지 않고 다른 앱에서 트리거한 위치 정보를 가져다 씀.
     */
    private fun setLocation() {
        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 업데이트 요청 설정
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(INTERVAL) // INTERVAL 마다 업데이트 요청

        // 위치 업데이트 리스너 생성
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                try{
                    /**
                     * W0D-74 1행 데이터 삭제
                     */
                    if(firstLocation != null){
                        locationResult.lastLocation?.let{
                            val location: Location = it
                            val timeStamp = location.time
                            /**
                             * WD-46 1행 데이터와 같은 데이터 삭제
                             */
                            if(firstLocation!!.latitude == location.latitude && firstLocation!!.longitude == location.longitude){
                                pastLocation = location
                                pastTimeStamp = timeStamp
                            } else{
                                /**
                                 * W0D-78 중복시간 삭제
                                 */
                                if(getDateFromTimeStampToSS(pastTimeStamp) != getDateFromTimeStampToSS(timeStamp)){
                                    /**
                                     * W0D-75 1초간 이동거리 70m 이상이면 삭제
                                     */
                                    if(pastLocation!=null){
                                        if((pastLocation!!.distanceTo(location) * 1000 / (timeStamp-pastTimeStamp)) > 70){
                                            pastLocation = location
                                            pastTimeStamp = timeStamp
                                        } else {
                                            processLocationCallback(location, timeStamp)
                                        }
                                    } else{
                                        processLocationCallback(location, timeStamp)
                                    }
                                }else{
                                    pastLocation = location
                                    pastTimeStamp = timeStamp
                                }
                            }
                        }
                    }else{
                        locationResult.lastLocation?.let{
                            firstLocation = it
                            pastLocation = it
                            pastTimeStamp = it.time
                        }
                    }
                }catch (e:Exception){
                    stopSensor()
                }
            }
        }

        // 위치 업데이트 요청 시작 전 퍼미션 체크
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        /**
         * 위치 업데이트 요청 시작
         */
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun processLocationCallback(location:Location, timeStamp:Long){
        /**
         * W0D-48 최후 종료 조건 추가
         * thirtyMinCheckpointLocation은 반경을 계산하기 위한 location 값
         */
        if(thirtyMinCheckpointLocation == null){
            thirtyMinCheckpointLocation = location
        }

        /**
         * 30분 간격으로 반경 체크 후 종료
         * 30분동안 반경 300m 이하 일 경우 종료
         * 60초 * 30분 = 1800
         */
        if((timeStamp - thirtyMinCheckpointLocation!!.time) > 1800000L){
            /**
             * 반경 300미터 이하 체크
             */
            if (maxDistance.max() < 300f) {
                /**
                 * pastMaxDistance.size가 0이라는건 30분 주행이라는 것. (첫번째 체크)
                 */
                if(pastMaxDistance.size == 0){
                    driveDatabase?.detectUserDao()?.insert(
                        DetectUserEntity(
                            user_id = "",
                            verification = "thirtyMinCheck",
                            start_stop = "stopSensorNotForSaving",
                            timestamp = System.currentTimeMillis().toString(),
                            sensor_state = fusedLocationClient != null
                        )
                    )
                    stopSensorNotForSaving()
                }else{
                    driveDatabase?.detectUserDao()?.insert(
                        DetectUserEntity(
                            user_id = "",
                            verification = "thirtyMinCheck",
                            start_stop = "stop",
                            timestamp = System.currentTimeMillis().toString(),
                            sensor_state = fusedLocationClient != null
                        )
                    )

                    stopSensor()
                }
            }else{
                pastMaxDistance = maxDistance.toMutableList()

                maxDistance = mutableListOf()
                thirtyMinCheckpointLocation = location
            }
        }

        maxDistance.add(location.distanceTo(thirtyMinCheckpointLocation!!))

        var distance = 0f
        if(pastLocation != null){
            distance = pastLocation!!.distanceTo(location)
        }

        val speed = location.speed * MS_TO_KH
        val acceleration = (location.speed * MS_TO_KH) - (pastSpeed * MS_TO_KH)

        gpsInfoForApp.add(EachGpsDtoForApp(timeStamp, location.latitude, location.longitude, String.format("%.0f", location.altitude).toDouble()))
        gpsInfoForApi.add(EachGpsDtoForApi(timeStamp, String.format("%.0f",speed).toFloat() ,String.format("%.0f",distance).toFloat(),String.format("%.0f", location.altitude).toDouble(), String.format("%.0f",acceleration).toFloat()))

        var HH = getDateFromTimeStampToHH(timeStamp)

        /**
         * 각 시간별 거리 계산
         */
        distance_array[HH] = distance_array[HH] + distance

        pastTimeStamp = timeStamp
        pastSpeed = location.speed
        pastLocation = location
    }

    private fun callApi(dataForApi:DriveForApi, dataForApp: DriveForApp){
        dataForApi.endTimestamp = System.currentTimeMillis()

        val postDriveDtoForApi = PostDrivingInfoRequest(
            userCarId=PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            startTimestamp = dataForApi.startTimestamp,
            endTimestamp = dataForApi.endTimestamp,
            verification = dataForApi.verification,
            gpses = dataForApi.gpses
        )

        val gson = Gson()
        val jsonParam = gson.toJson(postDriveDtoForApi)

        if (isInternetConnected(this@BluetoothService)) {
            apiService(this@BluetoothService).postDrivingInfo("Bearer " + PreferenceUtil.getPref(this@BluetoothService,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    try{
                        if(response.code() == 200 || response.code() == 201){
                            val postDrivingInfoResponse = gson.fromJson(response.body()?.string(), PostDrivingInfoResponse::class.java)
                            writeToRoomForApp(dataForApp, postDrivingInfoResponse.id)
                        }else if(response.code() == 429){

                        }else{
                            handlePostDrivingInfoError(dataForApi, dataForApp)
                        }
                    }catch (e:Exception){

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handlePostDrivingInfoError(dataForApi, dataForApp)
                }
            })
        } else {
            handlePostDrivingInfoError(dataForApi, dataForApp)
        }
    }

    private fun handlePostDrivingInfoError(dataForApi:DriveForApi, dataForApp: DriveForApp){
        writeToRoomForApi(dataForApi)
        writeToRoomForApp(dataForApp, dataForApi.tracking_id)
    }

    /**
     * App내 위경도 값 저장
     */
    fun writeToRoomForApp(dataForApp: DriveForApp, trackingId:String){
        Executors.newSingleThreadExecutor().execute{
            try {
                dataForApp.tracking_id = trackingId
                driveDatabase?.driveForAppDao()?.insert(dataForApp)
            } catch (e:Exception){

            }
        }
    }

    /**
     * API 호출실패 시 저장
     */
    fun writeToRoomForApi(driveForApi: DriveForApi){
        Executors.newSingleThreadExecutor().execute {
            try {
                driveDatabase?.driveForApiDao()?.insert(driveForApi)
            } catch (e:Exception){

            }
        }
    }

    private fun queryForState() {
        carConnectionQueryHandler.startQuery(
            QUERY_TOKEN,
            null,
            PROJECTION_HOST_URI,
            arrayOf(CAR_CONNECTION_STATE),
            null,
            null,
            null
        )
    }

    private fun scheduleWalkingDetectWork() {
        try {
            val workRequest = PeriodicWorkRequest.Builder(
                WalkingDetectWorker::class.java,
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WalkingDetectWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }catch (e:Exception){

        }
    }

    class WalkingDetectWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient =
            ActivityRecognition.getClient(context)

        override fun doWork(): Result {
            requestActivityUpdates()

            return Result.success()
        }

        private fun requestActivityUpdates() {
            val transitions = listOf(
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.STILL)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.STILL)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
            val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
            intent.setPackage(applicationContext.packageName)

            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            }

            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        ACTIVITY_RECOGNITION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
            }

            activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener { aVoid: Void? ->
                    Log.d("ActivityTransition", "Successfully removed previous updates")
                    // 2. 새로운 요청 등록
                    val request = ActivityTransitionRequest(transitions)
                    activityRecognitionClient
                        .requestActivityTransitionUpdates(request, pendingIntent)
                        .addOnSuccessListener { aVoid1: Void? ->
                            Log.d(
                                "ActivityTransition",
                                "Successfully requested new transitions"
                            )
                        }
                        .addOnFailureListener { e: java.lang.Exception? ->
                            Log.e(
                                "ActivityTransition",
                                "Request failed",
                                e
                            )
                        }
                }
                .addOnFailureListener { e: java.lang.Exception? ->
                    Log.e(
                        "ActivityTransition",
                        "Failed to remove previous updates",
                        e
                    )
                }
        }
    }

}