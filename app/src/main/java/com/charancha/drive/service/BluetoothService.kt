package com.charancha.drive.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothClass.Service.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.Sensor
import android.hardware.SensorEvent
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
import kotlin.concurrent.timerTask


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

    private var sensorState:Boolean = false

    private var driveDatabase: DriveDatabase? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient :FusedLocationProviderClient
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // 위치 업데이트 요청 설정
    private var sensorEventListener: SensorEventListener? = null

    private lateinit var fusedLocationClient2 :FusedLocationProviderClient
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest2: LocationRequest
    private lateinit var locationCallback2: LocationCallback

    /**
     * 위치 센서
     */
    private val gameRotationReading = FloatArray(3)
    private val geomagneticRotationReading = FloatArray(3)
    private val magneticReading = FloatArray(3)
    private val magneticUncalibratedReading = FloatArray(6)
    private val proximityReading = FloatArray(1)
    private val distanceBetween = FloatArray(3)
    private var pastLocation: Location? = null
    private var pastSpeed: Float = 0f

    /**
     * 움직임 감지 센서
     */
    private val accelerometerReading = FloatArray(3)
    private val accelerometerUncalibratedReading = FloatArray(6)
    private val gravityReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)
    private val gyroscopeUncalibratedReading = FloatArray(6)
    private val linearAccelerometerReading = FloatArray(3)
    private val rotationReading = FloatArray(4)

    /**
     * textFile 저장 용도
     */
    private var speedInfoFromGps: String = ""
    private var distanceInfoFromGps: String = ""
    private var distanceToInfoFromGps: String = ""
    private var pathLocationInfoFromGps: String = ""
    private var altitudeInfoFromGps: String = ""
    private var altitudeInfo: String = ""
    private var linearAccelerationInfo: String = ""
    private var rotationAngleInfo: String = ""
    private var inclineInfo: String = ""
    private var accelerationInfo: String = ""

    /**
     * 환경 센서
     */
    private var altitude = 0f
    private var writeTextPossible = false

    private var FASTEST_INTERVAL = 10000L
    private var INTERVAL = 20000L
    private val MAX_WAIT_TIME = 60000L

    private var INTERVAL2 = 60000L * 15

    /**
     *         locationRequest.setInterval(INTERVAL) // 20초마다 업데이트 요청
     *         locationRequest.setFastestInterval(FASTEST_INTERVAL) 다른 앱에서 연산된 위치를 수신
     *         setinterval() 메서드를 사용하여 앱을 위해 위치를 연산하는 간격을 지정합니다.
     *         setFastestInterval()을 사용하여 다른 앱에서 연산된 위치를 수신하는 간격을 지정합니다.
     */

    val MS_TO_KH = 3.6f

    lateinit var timer: Timer

    /**
     * room 데이터
     */
    lateinit var driveDto: DriveDto
    lateinit var gpsInfo: MutableList<EachGpsDto>
    private var maxSpeed: Float = 0f
    private var distanceSum: Float = 0f
    private var distanceToSum: Float = 0f
    private var startTimeStamp: Long = 0L



    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

        val CHANNEL_ID = "my_channel_02"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "check blueToothConnect",
            NotificationManager.IMPORTANCE_HIGH
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.btn_star_big_off)
            .setContentTitle("주행 관찰중..")
            .setContentText("주행 관찰중..").build()
        startForeground(1, notification)

        request = ActivityTransitionRequest(transitions)
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE)

        registerActivityTransitionUpdates()
        registerReceiver(transitionReceiver, filter)
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        setLocation2()
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

        }
    }


    private fun getCurrent(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportToFile("CONNECTION_TYPE_NOT_CONNECTED",getCurrent()+"\n\n")
                }else{
                    generateNoteOnSD("CONNECTION_TYPE_NOT_CONNECTED",getCurrent()+"\n\n")
                }
                stopSensor(L3)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportToFile("CONNECTION_TYPE_CONNECTED",getCurrent()+"\n\n")
                }else{
                    generateNoteOnSD("CONNECTION_TYPE_CONNECTED",getCurrent()+"\n\n")
                }

                startSensor(L3)
            }
        }
    }


    private fun startSensor(level:String){
        if(!sensorState){
            fusedLocationClient2.removeLocationUpdates(locationCallback2)

            sensorState = true
            PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
            driveDatabase = DriveDatabase.getDatabase(this)
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            initDriveData(level)
            setListener()
            setSensor()
            setLocation()
            setTimer()


        }
    }

    private fun stopSensor(level:String){
        if(sensorState){
            if(level == PreferenceUtil.getPref(this, PreferenceUtil.RUNNING_LEVEL, "")){
                sensorState = false

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()
                makeLinearAccelerationInfo()
                makeRotationVectorInfo()
                makeInclineInfo()
                makeAltitudeInfo()

                writeToRoom()

                timer.cancel()

                sensorManager.unregisterListener(sensorEventListener)
                fusedLocationClient.removeLocationUpdates(locationCallback)

                setLocation2()
            }
        }
    }

    private fun stopSensor(){
        if(sensorState){
            sensorState = false

            makeSpeedInfo()
            makeAccelerationInfo()
            makePathLocationInfo()
            makeDistanceBetween()
            makeAltitudeFromGpsInfo()
            makeLinearAccelerationInfo()
            makeRotationVectorInfo()
            makeInclineInfo()
            makeAltitudeInfo()

            writeToRoom()

            timer.cancel()

            sensorManager.unregisterListener(sensorEventListener)
            fusedLocationClient.removeLocationUpdates(locationCallback)

            setLocation2()

        }
    }

    private fun initDriveData(level:String){

        val format = SimpleDateFormat("yyyyMMddhhmmss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = Date()

        maxSpeed = 0f
        distanceSum = 0f
        distanceToSum = 0f
        startTimeStamp = System.currentTimeMillis()
        gpsInfo = mutableListOf()
        driveDto = DriveDto(format.format(time).toString(), startTimeStamp, level,0f,0L,0,0,gpsInfo)
    }


    inner class TransitionsReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                it.action?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        exportToFile(it,getCurrent()+"\n\n")
                    }else{
                        generateNoteOnSD(it,getCurrent()+"\n\n")
                    }
                }
            }

            if (ActivityTransitionResult.hasResult(intent)) {
                val result: ActivityTransitionResult = ActivityTransitionResult.extractResult(intent) ?: return
                for (event in result.transitionEvents) {
                    if(event.activityType == DetectedActivity.IN_VEHICLE){
                        if(event.transitionType.equals(ACTIVITY_TRANSITION_ENTER)){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("IN_VEHICLE ENTER",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("IN_VEHICLE ENTER" + getCurrent(),getCurrent()+"\n\n")
                            }

                            startSensor(L1)
                        } else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("IN_VEHICLE EXIT",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("IN_VEHICLE EXIT",getCurrent()+"\n\n")
                            }

                            stopSensor(L1)

                        }
                    } else if(event.activityType == DetectedActivity.WALKING){
                        if(event.transitionType.equals(ACTIVITY_TRANSITION_ENTER)){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("WALKING ENTER",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("WALKING ENTER",getCurrent()+"\n\n")
                            }

                            stopSensor()
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("Bluetooth AUDIO_VIDEO_HANDSFREE CONNECTED",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("Bluetooth AUDIO_VIDEO_HANDSFREE CONNECTED",getCurrent()+"\n\n")
                            }
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("Bluetooth AUDIO_VIDEO_HANDSFREE DISCONNECTED",getCurrent()+"\n\n")
                            }else{
                                generateNoteOnSD("Bluetooth AUDIO_VIDEO_HANDSFREE DISCONNECTED",getCurrent()+"\n\n")
                            }
                            stopSensor(L2)

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
            e.printStackTrace()
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

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun setTimer() {

        timer = Timer()
        timer.schedule(
            timerTask {

                writeTextPossible = true
            },
            0,
            INTERVAL
        )
    }

    private fun setListener() {
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                    System.arraycopy(
                        event.values,
                        0,
                        gameRotationReading,
                        0,
                        gameRotationReading.size
                    )

                    for ((index, value) in gameRotationReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_GAME_ROTATION_VECTOR :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
                    System.arraycopy(
                        event.values,
                        0,
                        geomagneticRotationReading,
                        0,
                        geomagneticRotationReading.size
                    )

                    for ((index, value) in geomagneticRotationReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_GEOMAGNETIC_ROTATION_VECTOR :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magneticReading, 0, magneticReading.size)

                    for ((index, value) in magneticReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_MAGNETIC_FIELD :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
                    System.arraycopy(
                        event.values,
                        0,
                        magneticUncalibratedReading,
                        0,
                        magneticUncalibratedReading.size
                    )

                    for ((index, value) in magneticUncalibratedReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_MAGNETIC_FIELD_UNCALIBRATED :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    System.arraycopy(event.values, 0, proximityReading, 0, proximityReading.size)

                    for ((index, value) in proximityReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_PROXIMITY :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(
                        event.values,
                        0,
                        accelerometerReading,
                        0,
                        accelerometerReading.size
                    )
                    for ((index, value) in accelerometerReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_ACCELEROMETER :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
                    System.arraycopy(
                        event.values,
                        0,
                        accelerometerUncalibratedReading,
                        0,
                        accelerometerUncalibratedReading.size
                    )

                    for ((index, value) in accelerometerUncalibratedReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_ACCELEROMETER_UNCALIBRATED :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                    System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
                    for ((index, value) in gravityReading.withIndex()) {
                        Log.d("onSensorChanged", "onSensorChanged TYPE_GRAVITY :: $index , $value")
                    }

                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                    for ((index, value) in gyroscopeReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_GYROSCOPE :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                    System.arraycopy(
                        event.values,
                        0,
                        gyroscopeUncalibratedReading,
                        0,
                        gyroscopeUncalibratedReading.size
                    )
                    for ((index, value) in gyroscopeUncalibratedReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_GYROSCOPE_UNCALIBRATED :: $index , $value"
                        )
                    }
                } else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    System.arraycopy(
                        event.values,
                        0,
                        linearAccelerometerReading,
                        0,
                        linearAccelerometerReading.size
                    )
                    for ((index, value) in linearAccelerometerReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_LINEAR_ACCELERATION :: $index , $value"
                        )
                    }

                } else if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    System.arraycopy(event.values, 0, rotationReading, 0, rotationReading.size)
                    for ((index, value) in rotationReading.withIndex()) {
                        Log.d(
                            "onSensorChanged",
                            "onSensorChanged TYPE_ROTATION_VECTOR :: $index , $value"
                        )
                    }
                } else if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                    altitude = event.values[0]
                }

                if (writeTextPossible) {
                    /**
                     * 기압
                     */
                    getAltitude()

                    /**
                     * 가속력
                     */
                    getAcceleration()

                    /**
                     * 회전 각도
                     */
                    getRotationAngle()

                    /**
                     * 기울기
                     */
                    if (magneticReading.isEmpty() || accelerometerReading.isEmpty()) return
                    getIncline()

                    writeTextPossible = false;
                }


            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

        }
    }


    private fun setSensor() {
        /**
         * 위치 센서
         */
        setGameRotation()
        setGeomagneticRotation()
        setMagneticField()
        setMagneticFieldUncalibrated()
        setProximity()


        /**
         * 움직임 감지 센서
         */
        setAccelerometer()
        setAccelerometerUncalibrated()
        setGravity()
        setGyroscope()
        setGyroscopeUncalibrated()
        setLinearAccelerometer()
        setRotation()

        /**
         * 환경 센서
         */
        setPressure()

    }

    private fun setGameRotation() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.also { gameRotation ->
            sensorManager.registerListener(
                sensorEventListener,
                gameRotation,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setGeomagneticRotation() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)?.also { geoVector ->
            sensorManager.registerListener(
                sensorEventListener,
                geoVector,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setMagneticField() {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                sensorEventListener,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setMagneticFieldUncalibrated() {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
            ?.also { magneticFieldUncalibrated ->
                sensorManager.registerListener(
                    sensorEventListener,
                    magneticFieldUncalibrated,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
    }

    private fun setProximity() {
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.also { proximity ->
            sensorManager.registerListener(
                sensorEventListener,
                proximity,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setAccelerometer() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setAccelerometerUncalibrated() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setGravity() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setGyroscope() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setGyroscopeUncalibrated() {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setLinearAccelerometer() {
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setRotation() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun setPressure() {
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)?.also {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
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
                    writeToFile("fused2",getCurrent() + "," + location.altitude + ", "+ location.longitude)
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
        fusedLocationClient2.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {

                }
            }

        fusedLocationClient2.requestLocationUpdates(
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
        locationRequest.setFastestInterval(FASTEST_INTERVAL)


//        locationRequest.setMaxWaitTime(MAX_WAIT_TIME)
//        locationRequest.setSmallestDisplacement(10f)


        // 위치 업데이트 리스너 생성
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("testestest","teststestests getCurrent :: " + getCurrent())

                try{
                    val location: Location = locationResult.lastLocation

                    getAltitude(location)
                    getPathLocation(location)
                    getSpeed(location)

                    var distance = 0f
                    if(pastLocation != null){
                        distance = pastLocation!!.distanceTo(location)
                    }

                    gpsInfo.add(EachGpsDto(System.currentTimeMillis(), location.latitude, location.longitude, location.speed,distance,location.altitude, (location.speed*MS_TO_KH) - (pastSpeed*MS_TO_KH)))

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
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("testeststs", "testsets altitude :: " + location.altitude)
                    Log.d("testeststs", "testsets latitude :: " + location.latitude)
                }
            }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
            accelerationInfo + getCurrent() + "," + ((location.speed) - (pastSpeed)) + "\n"

        pastSpeed = (location.speed)
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
            distanceToSum += distanceTo
        }

        pastLocation = location
    }

    private fun makePathLocationInfo() {
        writeToFile("Latitude, Longitude (gps)", pathLocationInfoFromGps)
    }

    private fun makeDistanceBetween(){
        writeToFile("Distance (gps)", "$distanceInfoFromGps\n\n distanceSum : $distanceSum \n\n")
    }


    /**
     * AltitudeFromGps
     */
    private fun getAltitude(location: Location) {
        altitudeInfoFromGps =
            altitudeInfoFromGps + getCurrent() + "," + location.altitude + "\n"
    }

    private fun makeAltitudeFromGpsInfo() {
        writeToFile("Altitude (gps)", altitudeInfoFromGps)
    }

    /**
     * TYPE_ACCELEROMETER
     */
    private fun getAcceleration() {
        linearAccelerationInfo = linearAccelerationInfo + getCurrent()
        var perAltitudeInfoFromGps = ""
        for ((index, value) in linearAccelerometerReading.withIndex()) {

            perAltitudeInfoFromGps = perAltitudeInfoFromGps + ",$value"
        }
        linearAccelerationInfo = linearAccelerationInfo + perAltitudeInfoFromGps + "\n"
    }

    private fun makeLinearAccelerationInfo() {
        writeToFile("Acceleration (acs)", linearAccelerationInfo)
    }

    /**
     * TYPE_ROTATION_VECTOR
     */
    private fun getRotationAngle() {
        rotationAngleInfo = rotationAngleInfo + getCurrent()
        var perRotationAngleInfo = ""
        for ((index, value) in rotationReading.withIndex()) {
            perRotationAngleInfo = perRotationAngleInfo + ",$value"
        }
        rotationAngleInfo = rotationAngleInfo + perRotationAngleInfo + "\n"
    }

    private fun makeRotationVectorInfo() {
        writeToFile("Rotation (gyro)", rotationAngleInfo)
    }


    /**
     * getOrientation
     */
    private fun getIncline() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(
            rotationMatrix,
            FloatArray(9),
            accelerometerReading,
            magneticReading
        )
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)


        inclineInfo = inclineInfo + getCurrent()
        var perInclineInfo = ""

        for ((index, value) in orientationAngles.withIndex()) {
            perInclineInfo = perInclineInfo + ", $value"
        }
        inclineInfo = inclineInfo + perInclineInfo + "\n"

    }

    private fun makeInclineInfo() {
        writeToFile("Incline (gyro)", inclineInfo)
    }


    /**
     * TYPE_PRESSURE
     */
    private fun getAltitude() {
        altitudeInfo =
            altitudeInfo + getCurrent() + "," + altitude + "\n"
    }

    private fun makeAltitudeInfo() {
        writeToFile("Pressure (baro)", altitudeInfo)
    }

    fun writeToFile(fileName: String, contents: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToFile(fileName, contents)
        } else{
            generateNoteOnSD(fileName, contents)
        }
    }

    fun writeToRoom(){
        driveDto.rawData = gpsInfo
        driveDto.distance = distanceSum
//        driveDto.maxSpeed = maxSpeed
        driveDto.time = System.currentTimeMillis() - startTimeStamp

        val drive = Drive(driveDto.tracking_id, driveDto.timeStamp, driveDto.rank, driveDto.distance, driveDto.time, driveDto.rapid1, driveDto.rapid2, Gson().toJson(driveDto))
        driveDatabase?.driveDao()?.insert(drive)
    }
}