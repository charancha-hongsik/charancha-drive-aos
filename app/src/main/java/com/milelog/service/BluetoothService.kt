package com.milelog.service

import android.Manifest
import android.app.*
import android.app.PendingIntent.*
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.database.Cursor
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.response.PostDrivingInfoResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.DriveForApp
import com.milelog.room.entity.DriveForApi
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.milelog.room.dto.EachGpsDtoForApi
import com.milelog.room.dto.EachGpsDtoForApp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * The Service will only run in one instance. However, everytime you start the service, the onStartCommand() method is called.
 */
class BluetoothService : Service() {
    companion object {
        const val TAG = "AutoConnectionDetector"

        // columnName for provider to query on connection status
        const val CAR_CONNECTION_STATE = "CarConnectionState"

        const val TRANSITIONS_RECEIVER_ACTION = "TRANSITIONS_RECEIVER_ACTION"
        const val TRANSITIONS_RECEIVER_ACTION2 = "TRANSITIONS_RECEIVER_ACTION2"



        // auto app on your phone will send broadcast with this action when connection state changes
        const val ACTION_CAR_CONNECTION_UPDATED = "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED"

        // phone is not connected to car
        const val CONNECTION_TYPE_NOT_CONNECTED = 0

        // phone is connected to Automotive OS
        const val CONNECTION_TYPE_NATIVE = 1

        // phone is connected to Android Auto
        const val CONNECTION_TYPE_PROJECTION = 2

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

    private var sensorState:Boolean = false
    private var driveDatabase: DriveDatabase? = null

    private var fusedLocationClient :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    /**
     * 위치 센서
     */
    private var pastLocation: Location? = null
    private var firstLineLocation: Location? = null
    private var pastSpeed: Float = 0f
    private var pastTimeStamp = 0L


    private var INTERVAL = 1000L

    /**
     *         locationRequest.setInterval(INTERVAL) // 20초마다 업데이트 요청
     *         locationRequest.setFastestInterval(FASTEST_INTERVAL) 다른 앱에서 연산된 위치를 수신
     *         setinterval() 메서드를 사용하여 앱을 위해 위치를 연산하는 간격을 지정합니다.
     *         setFastestInterval()을 사용하여 다른 앱에서 연산된 위치를 수신하는 간격을 지정합니다.
     */

    val MS_TO_KH = 3.6f

    /**
     * room 데이터
     */
    lateinit var driveForApp: DriveForApp
    lateinit var gpsInfoForApp: MutableList<EachGpsDtoForApp>

    lateinit var driveForApi: DriveForApi
    lateinit var gpsInfoForApi: MutableList<EachGpsDtoForApi>

    private var startTimeStamp: Long = 0L

    /**
     *  타이머 시간동안 최대 반경을 구하기 위한 변수들
     */
    var firstLocation: Location? = null
    var maxDistance = mutableListOf<Float>()
    var pastMaxDistance = mutableListOf<Float>()


    var firstLineState = false

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

    override fun onTaskRemoved(rootIntent: Intent?) {
        scheduleForegroundServiceStart(this@BluetoothService)
        super.onTaskRemoved(rootIntent)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!sensorState){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(TransitionsReceiver(), filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(TransitionsReceiver(), filter)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Detecting L1 Receiver
                registerReceiver(WalkingDetectReceiver(), IntentFilter().apply {
                    addAction(TRANSITIONS_RECEIVER_ACTION)
                }, RECEIVER_EXPORTED)
            } else {
                // Detecting L1 Receiver
                registerReceiver(WalkingDetectReceiver(), IntentFilter().apply {
                    addAction(TRANSITIONS_RECEIVER_ACTION)
                })
            }

            carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            notification = NotificationCompat.Builder(this, CHANNEL_ID)

            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notification
                    .setSmallIcon(R.mipmap.ic_notification)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentText("주행 관찰중이에요.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
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

            scheduleWalkingDetectWork()
        }

        return START_STICKY
    }

    override fun onCreate() {
        // Detecting L2/L3 Receiver
        super.onCreate()
    }

    fun refreshNotiText(){
        if(sensorState){
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 중..").build())
        }else{
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중이에요.").build())
        }
    }

    class WalkingDetectReceiver : BroadcastReceiver() {

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
                                if((context as BluetoothService).sensorState){
                                    context.stopSensor()
                                }
                            }
                        } else if(activityType == DetectedActivity.IN_VEHICLE){
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                // Vehicle 활동에 들어감
                                (context as BluetoothService).startSensor(L1)
                            }
                        }
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
                stopSensor(L3)
            } else {
                startSensor(L3)
            }
        }
    }

    /**
     * sensor가 이미 켜져있으면 켜지지않음.
     */
    fun startSensor(level:String){
        try {
            if (!sensorState) {
                sensorState = true

                /**
                 * W0D-74 1행 데이터 삭제
                 */
                firstLineState = true
                firstLineLocation = null

                PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
                driveDatabase = DriveDatabase.getDatabase(this)
                initDriveData(level)
                setLocation()

                refreshNotiText()
            }
        } catch(e:Exception){

        }
    }

    /**
     * sensor 상태가 On일 때 끌 수 있음.
     * level이 같아야 끌 수 있음.
     */
    private fun stopSensor(level:String){
        try {
            if (sensorState) {
                if (level == PreferenceUtil.getPref(this, PreferenceUtil.RUNNING_LEVEL, "")) {
                    firstLineState = false
                    firstLineLocation = null
                    firstLocation = null
                    maxDistance = mutableListOf()
                    pastMaxDistance = mutableListOf()

                    if(distance_array.sum() > 500f){
                        callApi()
                    }else{
                        sensorState = false
                    }

                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                    fusedLocationClient = null

                    refreshNotiText()
                }
            }
        }catch (e:Exception){
        }
    }

    fun stopSensor(){
        try {
            if (sensorState) {
                firstLineState = false
                firstLineLocation = null
                firstLocation = null
                maxDistance = mutableListOf()
                pastMaxDistance = mutableListOf()

                if(distance_array.sum() > 500f){
                    callApi()
                }else{
                    sensorState = false
                }

                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null

                refreshNotiText()
            }
        }catch(e:Exception){

        }
    }

    private fun initDriveData(level:String){

        distance_array = MutableList(24) { 0f } // 23개 시간대의 distance

        startTimeStamp = System.currentTimeMillis()

        gpsInfoForApp = mutableListOf()
        driveForApp = DriveForApp(
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!! + startTimeStamp,
            gpsInfoForApp)


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

    inner class TransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(ContextCompat.checkSelfPermission(this@BluetoothService, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
                if(intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED){
                    val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter


                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                    pairedDevices?.forEach { device ->
                        if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE){
                            if(isConnected(device)){
                                startSensor(L2)
                            }
                        }
                    }

                } else if(intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED){
                    val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                    pairedDevices?.forEach { device ->
                        if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE){
                            if(!isConnected(device)){
                                stopSensor(L2)
                            }
                        }
                    }
                } else{
                    queryForState()
                }
            }
        }
    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean

            return m.invoke(device) as Boolean
        } catch (e:Exception){
            return false
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

    class restartForegroundServiceWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

        override fun doWork(): Result {

            if(!(context as BluetoothService).sensorState){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(context.TransitionsReceiver(), context.filter, RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(context.TransitionsReceiver(), context.filter)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Detecting L1 Receiver
                    context.registerReceiver(WalkingDetectReceiver(), IntentFilter().apply {
                        addAction(TRANSITIONS_RECEIVER_ACTION)
                    }, RECEIVER_EXPORTED)
                } else {
                    // Detecting L1 Receiver
                    context.registerReceiver(WalkingDetectReceiver(), IntentFilter().apply {
                        addAction(TRANSITIONS_RECEIVER_ACTION)
                    })
                }

                context.carConnectionQueryHandler = context.CarConnectionQueryHandler(context.contentResolver)

                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    context.channel
                )
                context.notification = NotificationCompat.Builder(context, context.CHANNEL_ID)

                if (Build.VERSION.SDK_INT >= 34) {
                    context.startForeground(1, context.notification
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentText("주행 관찰중이에요.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOnlyAlertOnce(true)
                        .build(), FOREGROUND_SERVICE_TYPE_HEALTH)
                }else{
                    context.startForeground(1, context.notification
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentText("주행 관찰중이에요.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOnlyAlertOnce(true)
                        .build())
                }

                context.scheduleWalkingDetectWork()
            }

            return Result.success()
        }
    }


    fun scheduleForegroundServiceStart(context: Context) {
        // 백그라운드에서 Foreground Service를 시작하도록 작업을 예약
        val workRequest = OneTimeWorkRequest.Builder(restartForegroundServiceWorker::class.java)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
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
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.ON_BICYCLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build(),
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.ON_FOOT)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )

            val request = ActivityTransitionRequest(transitions)

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
                        Manifest.permission.ACTIVITY_RECOGNITION
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

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d("testestestest","testestestest activityRecognitionClient addOnSuccessListener")
                }
                .addOnFailureListener {

                }
        }
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
                    if(!firstLineState){
                        locationResult.lastLocation?.let{
                            val location: Location = it
                            val timeStamp = location.time

                            /**
                             * WD-46 1행 데이터와 같은 데이터 삭제
                             */
                            if(firstLineLocation != null){
                                if(firstLineLocation!!.latitude == location.latitude && firstLineLocation!!.longitude == location.longitude){
                                    pastLocation = location
                                    pastTimeStamp = location.time
                                } else{
                                    /**
                                     * W0D-78 중복시간 삭제
                                     */

                                    if(getDateFromTimeStampToSS(pastTimeStamp) != getDateFromTimeStampToSS(timeStamp)){

                                        /**
                                         * W0D-75 1초간 이동거리 70m 이상이면 제외
                                         */

                                        if(pastLocation!=null){
                                            if((pastLocation!!.distanceTo(location) * 1000 / (timeStamp-pastTimeStamp)) > 70){
                                                pastTimeStamp = timeStamp
                                                pastLocation = location
                                            } else {
                                                processLocationCallback(location, timeStamp)
                                            }
                                        } else{
                                            processLocationCallback(location, timeStamp)
                                        }
                                    }else{
                                        pastLocation = location
                                        pastTimeStamp = location.time

                                    }
                                }
                            }
                        }
                    }else{
                        locationResult.lastLocation?.let{
                            firstLineState = false
                            firstLineLocation = it
                            pastLocation = it
                            pastTimeStamp = it.time
                        }
                    }
                }catch (e:Exception){

                }
            }
        }

        // 위치 업데이트 요청 시작
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

        // 마지막 위치 처리
        fusedLocationClient?.lastLocation!!
            .addOnSuccessListener { location: Location? ->
                if (location != null) {

                }
            }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun processLocationCallback(location:Location, timeStamp:Long){
        /**
         * W0D-48 최후 종료 조건 추가
         * firstLocation은 반경을 계산하기 위한 location 값
         */
        if(firstLocation == null){
            firstLocation = location
        }

        if(pastLocation != null){
            for(i in 1..(location.time-pastLocation!!.time)/1000) {
                maxDistance.add(location.distanceTo(firstLocation!!))
            }
        }

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
         * 거리 계산
         */
        distance_array[HH] = distance_array[HH] + distance

        /**
         * 30분 간격으로 체크
         */
        if(maxDistance.size > 1800){
            /**
             * 반경 300미터 이하 체크
             */
            if (maxDistance.max() < 300f) {
                stopSensor()

                maxDistance = mutableListOf()
                pastMaxDistance = mutableListOf()
                firstLocation = null

            }else{
                maxDistance = mutableListOf()
                firstLocation = null
                pastMaxDistance = maxDistance.toMutableList()
            }
        }

        pastTimeStamp = timeStamp
        pastSpeed = location.speed
        pastLocation = location
    }

    private fun callApi(){

        driveForApi.endTimestamp = System.currentTimeMillis()

        val postDriveDtoForApi = PostDrivingInfoRequest(
            userCarId=PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            startTimestamp = driveForApi.startTimestamp,
            endTimestamp = driveForApi.endTimestamp,
            verification = driveForApi.verification,
            gpses = driveForApi.gpses
        )

        val gson = Gson()
        val jsonParam = gson.toJson(postDriveDtoForApi)

        if (isInternetConnected(this@BluetoothService)) {
            apiService().postDrivingInfo("Bearer " + PreferenceUtil.getPref(this@BluetoothService,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200 || response.code() == 201){
                        val postDrivingInfoResponse = gson.fromJson(response.body()?.string(), PostDrivingInfoResponse::class.java)
                        writeToRoomForApp(postDrivingInfoResponse.id)
                    }else{
                        writeToRoomForApi(driveForApi)
                        writeToRoomForApp(driveForApi.tracking_id)
                    }

                    sensorState = false

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    writeToRoomForApi(driveForApi)
                    writeToRoomForApp(driveForApi.tracking_id)

                    sensorState = false
                }
            })

        } else {
            writeToRoomForApi(driveForApi)
            writeToRoomForApp(driveForApi.tracking_id)
            sensorState = false

        }
    }

    fun writeToRoomForApp(trackingId:String){
        Executors.newSingleThreadExecutor().execute{
            try {
                driveForApp.tracking_id = trackingId
                driveDatabase?.driveForAppDao()?.insert(driveForApp)
            } catch (e:Exception){
            }
        }
    }

    fun writeToRoomForApi(driveForApi: DriveForApi){
        Executors.newSingleThreadExecutor().execute {
            try {
                driveDatabase?.driveForApiDao()?.insert(driveForApi)
            } catch (e:Exception){
            }
        }
    }


    private fun getDateFromTimeStampToHH(timeStamp:Long) : Int{
        val format = SimpleDateFormat("HH")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    private fun getDateFromTimeStampToSS(timeStamp:Long) : Int{
        val format = SimpleDateFormat("ss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    fun apiService(): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(this))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_API_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}