package com.charancha.drive.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothClass.Service.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.calculateData
import com.charancha.drive.room.DriveDto
import com.charancha.drive.room.EachGpsDto
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.room.entity.Drive
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer


/**
 * The Service will only run in one instance. However, everytime you start the service, the onStartCommand() method is called.
 */
class BluetoothService : Service() {
    companion object {
        const val TRANSITIONS_RECEIVER_ACTION = "TRANSITIONS_RECEIVER_ACTION"

        const val TAG = "AutoConnectionDetector"

        // columnName for provider to query on connection status
        const val CAR_CONNECTION_STATE = "CarConnectionState"

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

    private val transitions: List<ActivityTransition> by lazy {
        listOf(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
    }
    private val transitionReceiver by lazy {
        TransitionsReceiver()
    }

    val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(TRANSITIONS_RECEIVER_ACTION)
        addAction(ACTION_CAR_CONNECTION_UPDATED)
    }

    private lateinit var request: ActivityTransitionRequest
    private lateinit var pendingIntent: PendingIntent

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ SensorService 관련 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    var distance_array = MutableList(23) { 0f } // 23개 시간대의 distance
    var sudden_deceleration_array = MutableList(23) { 0 } // 23개 시간대의 sudden_deceleration 갯수
    var sudden_stop_array = MutableList(23) { 0 } // 23개 시간대의 sudden_stop 갯수
    var sudden_acceleration_array = MutableList(23) { 0 }// 23개 시간대의 sudden_acceleration 갯수
    var sudden_start_array= MutableList(23) { 0 }  // 23개 시간대의 sudden_start 갯수
    var high_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 high_speed_driving 거리
    var low_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 low_speed_driving 거리
    var constant_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 constant_speed_driving 거리
    var harsh_driving_array = MutableList(23) { 0f } // 23개 시간대의 harsh_driving 거리
    var sumSuddenDecelerationDistance = 0f

    private var sensorState:Boolean = false

    private var driveDatabase: DriveDatabase? = null

    private lateinit var sensorManager: SensorManager
    private var fusedLocationClient :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // 위치 업데이트 요청 설정
    private var sensorEventListener: SensorEventListener? = null

    private var fusedLocationClient2 :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest2: LocationRequest
    private lateinit var locationCallback2: LocationCallback

    /**
     * 위치 센서
     */
    private val distanceBetween = FloatArray(3)
    private var pastLocation: Location? = null
    private var pastSpeed: Float = 0f
    private var pastTimeStamp = 0L


    /**
     * textFile 저장 용도
     */
    private var speedInfoFromGps: String = ""
    private var distanceInfoFromGps: String = ""
    private var distanceToInfoFromGps: String = ""
    private var pathLocationInfoFromGps: String = ""
    private var altitudeInfoFromGps: String = ""
    private var accelerationInfo: String = ""


    private var INTERVAL = 1000L
    private var INTERVAL2 = 60000L * 5

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
    lateinit var driveDto: DriveDto
    lateinit var gpsInfo: MutableList<EachGpsDto>
    private var maxSpeed: Float = 0f
    private var distanceSum: Float = 0f
    private var startTimeStamp: Long = 0L

    /**
     *  타이머 시간동안 최대 반경을 구하기 위한 변수들
     */
    lateinit var distanceSumForAnHourTimer:Timer
    lateinit var alarmTimer:Timer
    var firstLocation: Location? = null
    var maxDistance = 0f

    var firstLineState = false

    /**
     * notification 관련
     */

    lateinit var notification: NotificationCompat.Builder
    val CHANNEL_ID = "my_channel_02"
    val channel = NotificationChannel(
        CHANNEL_ID,
        "check blueToothConnect",
        NotificationManager.IMPORTANCE_HIGH
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

        request = ActivityTransitionRequest(transitions)

        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE)


        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        notification = NotificationCompat.Builder(this, CHANNEL_ID)


        startForeground(1, notification.setSmallIcon(android.R.drawable.btn_star_big_off)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentText("주행 관찰중.." + getCurrent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build())

        setLocation2()

        sensorState = false

//         주기적으로 알림 갱신
        alarmTimer = timer(period = 600000, initialDelay = 600000 ) {
            if(sensorState){
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 중..($distanceSum m)").build())
            } else{
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중.." + getCurrent()).build())
            }
        }

        registerActivityTransitionUpdates()
        registerReceiver(transitionReceiver, filter)

        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()
    }


    private fun registerActivityTransitionUpdates() {
        ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)
            .addOnSuccessListener {

            }.addOnFailureListener { e ->

            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToFile(fileName: String, content: String) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")

            // query for the file
            val cursor: Cursor? = contentResolver.query(
                extVolumeUri,
                arrayOf(MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads._ID),
                null,
                null,
                null
            )


            var fileUri: Uri? = null

            // if file found
            if (cursor != null && cursor.count > 0) {
                // get URI
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (nameIndex > -1) {
                        val displayName = cursor.getString(nameIndex)
                        if (displayName == "$fileName.txt") {
                            val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                            if (idIndex > -1) {
                                val id = cursor.getLong(idIndex)
                                fileUri = ContentUris.withAppendedId(extVolumeUri, id)
                            }
                        }
                    }
                }

                cursor.close()
            } else {
                // insert new file otherwise
                fileUri = contentResolver.insert(extVolumeUri, contentValues)
            }

            if (fileUri == null) {
                fileUri = contentResolver.insert(extVolumeUri, contentValues)
            }

            if (fileUri != null) {
                val os = contentResolver.openOutputStream(fileUri, "wa")

                if (os != null) {
                    os.write(content.toByteArray())
                    os.close()
                }
            }
        }catch (e:Exception){
            writeToFile("exception1",e.toString())
        }
    }


    private fun getCurrent(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = Date()
        return format.format(time)
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
    private fun startSensor(level:String){
        try {
            if (!sensorState) {
                /**
                 * W0D-74 1행 데이터 삭제
                 */
                firstLineState = true
                startDistanceTimer()

                sensorState = true
                PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
                driveDatabase = DriveDatabase.getDatabase(this)
                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
                initDriveData(level)
                setLocation()
            }
        } catch(e:Exception){
            writeToFile("exception5",e.toString())
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
                    sensorState = false
                    firstLineState = false

                    makeSpeedInfo()
                    makeAccelerationInfo()
                    makePathLocationInfo()
                    makeDistanceBetween()
                    makeAltitudeFromGpsInfo()

                    writeToRoom()

                    stopDistanceTimer()

                    sensorManager.unregisterListener(sensorEventListener)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                    fusedLocationClient = null
                }
            }
        }catch (e:Exception){
            writeToFile("exception2",e.toString())

        }
    }

    private fun stopSensor(){
        try {
            if (sensorState) {
                sensorState = false
                firstLineState = false

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()

                writeToRoom()

                stopDistanceTimer()

                sensorManager.unregisterListener(sensorEventListener)
                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null

            }
        }catch(e:Exception){
            writeToFile("exception3",e.toString())

        }
    }

    private fun initDriveData(level:String){

        val format = SimpleDateFormat("yyyyMMddHHmmss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = Date()

        speedInfoFromGps = ""
        distanceInfoFromGps = ""
        distanceToInfoFromGps = ""
        pathLocationInfoFromGps = ""
        altitudeInfoFromGps = ""
        accelerationInfo = ""

        distance_array = MutableList(23) { 0f } // 23개 시간대의 distance
        sudden_deceleration_array = MutableList(23) { 0 } // 23개 시간대의 sudden_deceleration 갯수
        sudden_stop_array = MutableList(23) { 0 } // 23개 시간대의 sudden_stop 갯수
        sudden_acceleration_array = MutableList(23) { 0 }// 23개 시간대의 sudden_acceleration 갯수
        sudden_start_array = MutableList(23) { 0 }  // 23개 시간대의 sudden_start 갯수
        high_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 high_speed_driving 거리
        low_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 low_speed_driving 거리
        constant_speed_driving_array = MutableList(23) { 0f } // 23개 시간대의 constant_speed_driving 거리
        harsh_driving_array = MutableList(23) { 0f } // 23개 시간대의 harsh_driving 거리
        sumSuddenDecelerationDistance = 0f

        maxSpeed = 0f
        distanceSum = 0f
        startTimeStamp = System.currentTimeMillis()
        gpsInfo = mutableListOf()
        driveDto = DriveDto(
            format.format(time).toString(),
            startTimeStamp,
            level,
            listOf(),0L, listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            0f,
            gpsInfo)
    }


    inner class TransitionsReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result: ActivityTransitionResult = ActivityTransitionResult.extractResult(intent) ?: return
                for (event in result.transitionEvents) {
                    if(event.activityType == DetectedActivity.IN_VEHICLE){
                        if(event.transitionType.equals(ACTIVITY_TRANSITION_ENTER)){
                            startSensor(L1)
                            writeToFile("IN_VEHICLE", getCurrent())
                        }
                    } else if(event.activityType == DetectedActivity.WALKING){
                        if(event.transitionType.equals(ACTIVITY_TRANSITION_ENTER)){
                            stopSensor()
                            writeToFile("Walking", getCurrent())
                        }
                    }
                }
            } else if(intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED){
                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                pairedDevices?.forEach { device ->
                    if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_HANDSFREE){
                        if(isConnected(device)){
                            startSensor(L2)
                            writeToFile("HANDSFREE ON", getCurrent())
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
                            writeToFile("HANDSFREE OFF", getCurrent())
                        }
                    }
                }
            } else{
                queryForState()
            }
        }
    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    fun generateNoteOnSD(sFileName: String?, sBody: String?) {
        try {
            val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Notes")
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, "$sFileName.txt")
            val writer = FileWriter(gpxfile)
            writer.append(sBody)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            writeToFile("exception4",e.toString())
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

    private fun startDistanceTimer(){
        maxDistance = 0f
        firstLocation = null
        distanceSumForAnHourTimer = timer(period = 3600000, initialDelay = 3600000) {
            // 타이머가 동작 중인 동안 1시간 동안의 거리를 합산

            if(maxDistance <= 300f){
                stopSensor()
            }
            maxDistance = 0f
            firstLocation = null
        }
    }

    private fun stopDistanceTimer(){
        distanceSumForAnHourTimer.cancel()
    }


    private fun setLocation2() {
        // FusedLocationProviderClient 초기화
        fusedLocationClient2 = LocationServices.getFusedLocationProviderClient(this)

        // 위치 업데이트 요청 설정
        locationRequest2 = LocationRequest.create()
        locationRequest2.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        locationRequest2.setInterval(INTERVAL2) // INTERVAL 마다 업데이트 요청


        // 위치 업데이트 리스너 생성
        locationCallback2 = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                try{
                    val location: Location = locationResult.lastLocation
//                    writeToFile("fused2",getCurrent() + "," + location.altitude + ", "+ location.longitude + "\n")
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
        fusedLocationClient2?.lastLocation!!
            .addOnSuccessListener { location: Location? ->
                if (location != null) {

                }
            }

        fusedLocationClient2?.requestLocationUpdates(
            locationRequest2,
            locationCallback2,
            Looper.getMainLooper()
        )
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

                        val location: Location = locationResult.lastLocation
                        val timeStamp = System.currentTimeMillis()

                        /**
                         * W0D-78 중복시간 삭제
                         */
                        if(getDateFromTimeStampToSS(pastTimeStamp) != getDateFromTimeStampToSS(timeStamp)){

                            /**
                             * W0D-75 1초간 이동거리 70m 이상이면 제외
                             */
                            if(pastLocation!=null){
                                if((pastLocation!!.distanceTo(location) / ((timeStamp-pastTimeStamp)/1000)) > 70){
                                    writeToFile("deleted path","distance : " + pastLocation!!.distanceTo(location))
                                    pastTimeStamp = timeStamp
                                    pastLocation = location
                                } else {
                                    processLocationCallback(location, timeStamp)
                                }
                            } else{
                                processLocationCallback(location, timeStamp)
                            }
                        }else{
                            if(pastLocation != null){
                                writeToFile("deleted path","distance : " + pastLocation!!.distanceTo(location))
                            }
                            pastTimeStamp = timeStamp
                            pastLocation = location
                        }

                    }else{
                        firstLineState = false
                        pastTimeStamp = System.currentTimeMillis()
                        pastLocation = locationResult.lastLocation
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
                    Log.d("testeststs", "testsets altitude :: " + location.altitude)
                    Log.d("testeststs", "testsets latitude :: " + location.latitude)
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
        if(location.distanceTo(firstLocation!!) > maxDistance){
            maxDistance = location.distanceTo(firstLocation!!)
        }

        getAltitude(location)
        getPathLocation(location)
        getSpeed(location)

        var distance = 0f
        if(pastLocation != null){
            distance = pastLocation!!.distanceTo(location)
        }

        gpsInfo.add(EachGpsDto(timeStamp, location.latitude, location.longitude, location.speed,distance,location.altitude, (location.speed) - (pastSpeed)))

        var HH = getDateFromTimeStamp(timeStamp)

        /**
         * 거리 계산
         */
        distance_array[HH] = distance_array[HH] + distance


        /**
         * 현재 시간 - 이전 시간 = 2초 이상이면, 데이터 제외한다.
         * 이전 속력, 현재 속력이 0인 경우 데이터 제외한다.
         */
        if(timeStamp-pastTimeStamp < 2000){
            if(location.speed != 0f){
                if (pastLocation?.speed != 0f){
                    /**
                     * 급감속 계산
                     */
                    if (location.speed * calculateData.MS_TO_KH >= 6f && ((location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)) <= -14f) {
                        sudden_deceleration_array[HH]++
                        harsh_driving_array[HH] = harsh_driving_array[HH] + distance
                        sumSuddenDecelerationDistance += distance

                    }

                    /**
                     * 급가속 계산
                     */
                    if (location.speed * calculateData.MS_TO_KH >= 10f && ((location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)) >= 10f) {
                        sudden_acceleration_array[HH]++
                        harsh_driving_array[HH] = harsh_driving_array[HH] + distance
                        sumSuddenDecelerationDistance += distance
                    }


                    /**
                     * 급정지 계산
                     */
                    if (location.speed * calculateData.MS_TO_KH >= 5f && ((location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)) <= -14f) {
                        sudden_stop_array[HH]++
                        harsh_driving_array[HH] = harsh_driving_array[HH] + distance
                    }

                    /**
                     * 급출발 계산
                     */
                    if (location.speed * calculateData.MS_TO_KH <= 5f && ((location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)) >= 10f) {
                        sudden_start_array[HH]++
                        harsh_driving_array[HH] = harsh_driving_array[HH] + distance
                    }

                }
            }
        }

        /**
         * 고속주행 거리 계산
         */
        if (location.speed * calculateData.MS_TO_KH in 80f..150f) {
            high_speed_driving_array[HH] = high_speed_driving_array[HH] + distance
        }

        /**
         * 저속주행 거리 계산
         */
        if (location.speed * calculateData.MS_TO_KH in 0f..39.9999f) {
            low_speed_driving_array[HH] = low_speed_driving_array[HH] + distance
        }


        pastTimeStamp = timeStamp
        pastSpeed = location.speed
        pastLocation = location
    }

    /**
     * SpeedFromGps
     */
    private fun getSpeed(location: Location) {
        speedInfoFromGps =
            speedInfoFromGps + getCurrent() + "," + (location.speed*MS_TO_KH) + "\n"

        if(maxSpeed < location.speed*MS_TO_KH){
            maxSpeed = location.speed*MS_TO_KH
        }

        accelerationInfo =
            accelerationInfo + getCurrent() + "," + ((location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)) + "\n"
    }

    private fun makeSpeedInfo() {
        writeToFile("speed (gps)", "$speedInfoFromGps\n\n maxSpeed : $maxSpeed \n\n")
    }

    private fun makeAccelerationInfo() {
        writeToFile("Acceleration (gps)", accelerationInfo)
    }

    /**
     * pathLocationInfoFromGps
     */
    private fun getPathLocation(location: Location) {
        pathLocationInfoFromGps =
            pathLocationInfoFromGps + getCurrent() + "," + location.latitude + "," + location.longitude + "\n"

        if(pastLocation != null){
            Location.distanceBetween(location.latitude, location.longitude, pastLocation!!.latitude, pastLocation!!.longitude, distanceBetween)
            val distanceTo: Float = pastLocation!!.distanceTo(location)

            distanceInfoFromGps =
                distanceInfoFromGps + getCurrent() + "," + distanceBetween[0] + "\n"

            distanceToInfoFromGps =
                distanceToInfoFromGps + getCurrent() + "," + distanceTo + "\n"

            distanceSum += distanceBetween[0]
        }
    }

    private fun makePathLocationInfo() {
        writeToFile("Latitude, Longitude (gps)", pathLocationInfoFromGps)
    }

    private fun makeDistanceBetween(){
        writeToFile("Distance (gps)", "$distanceInfoFromGps\n\n distanceSum : $distanceSum \n\n")
    }

    private fun getAltitude(location: Location) {
        altitudeInfoFromGps =
            altitudeInfoFromGps + getCurrent() + "," + location.altitude + "\n"
    }

    private fun makeAltitudeFromGpsInfo() {
        writeToFile("Altitude (gps)", altitudeInfoFromGps)
    }

    fun writeToFile(fileName: String, contents: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToFile(fileName, contents)
        } else{
            generateNoteOnSD(fileName, contents)
        }
    }

    fun writeToRoom(){
        Thread{
            try {
                driveDto.jsonData = gpsInfo
                driveDto.time = System.currentTimeMillis() - startTimeStamp

                val drive = Drive(
                    driveDto.tracking_id,
                    driveDto.timeStamp,
                    driveDto.verification,
                    distance_array.toList(),
                    driveDto.time,
                    sudden_deceleration_array.toList(),
                    sudden_stop_array.toList(),
                    sudden_acceleration_array.toList(),
                    sudden_start_array.toList(),
                    high_speed_driving_array.toList(),
                    low_speed_driving_array.toList(),
                    calculateData.getConstantSpeedDriving(gpsInfo),
                    harsh_driving_array.toList(),
                    sumSuddenDecelerationDistance,
                    gpsInfo)

                driveDatabase?.driveDao()?.insert(drive)
            } catch (e:Exception){
                writeToFile("exception",e.toString())
            }
        }.start()
    }

    private fun getDateFromTimeStamp(timeStamp:Long) : Int{
        val format = SimpleDateFormat("HH")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    private fun getDateFromTimeStampToSS(timeStamp:Long) : Int{
        val format = SimpleDateFormat("ss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }


}