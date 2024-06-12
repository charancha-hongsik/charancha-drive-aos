package com.charancha.drive.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.*
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothClass.Service.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.calculateData
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.room.dto.DriveDto
import com.charancha.drive.room.dto.DriveDtoForApi
import com.charancha.drive.room.dto.EachGpsDto
import com.charancha.drive.room.dto.EachGpsDtoForApi
import com.charancha.drive.room.entity.Drive
import com.charancha.drive.room.entity.DriveForApi
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
    var sudden_deceleration_array = MutableList(24) { 0 } // 24개 시간대의 sudden_deceleration 갯수
    var sudden_stop_array = MutableList(24) { 0 } // 24개 시간대의 sudden_stop 갯수
    var sudden_acceleration_array = MutableList(24) { 0 }// 24개 시간대의 sudden_acceleration 갯수
    var sudden_start_array= MutableList(24) { 0 }  // 24개 시간대의 sudden_start 갯수
    var high_speed_driving_array = MutableList(24) { 0f } // 24개 시간대의 high_speed_driving 거리
    var low_speed_driving_array = MutableList(24) { 0f } // 24개 시간대의 low_speed_driving 거리
    var constant_speed_driving_array = MutableList(24) { 0f } // 24개 시간대의 constant_speed_driving 거리
    var harsh_driving_array = MutableList(24) { 0f } // 24개 시간대의 harsh_driving 거리
    var sumSuddenDecelerationDistance = 0f

    var constantList1 = MutableList(24) {0f}
    var constantList2 = MutableList(24) {0f}
    var constantList3 = MutableList(24) {0f}
    var constantList4 = MutableList(24) {0f}
    var constantList5 = MutableList(24) {0f}
    var firstConstantTimeStamp = 0L

    private var sensorState:Boolean = false

    private var driveDatabase: DriveDatabase? = null

    private var fusedLocationClient :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var fusedLocationClient2 :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest2: LocationRequest
    private lateinit var locationCallback2: LocationCallback


    /**
     * 위치 센서
     */
    private val distanceBetween = FloatArray(3)
    private var pastLocation: Location? = null
    private var firstLineLocation: Location? = null
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
    private var INTERVAL2 = 180000L

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
    lateinit var driveDtoForApi: DriveDtoForApi
    lateinit var gpsInfoForApi: MutableList<EachGpsDtoForApi>
    private var maxSpeed: Float = 0f
    private var distanceSum: Float = 0f
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
        "check blueToothConnect",
        NotificationManager.IMPORTANCE_HIGH
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!sensorState){
            carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            notification = NotificationCompat.Builder(this, CHANNEL_ID)

            setLocation2()


            startForeground(1, notification.setSmallIcon(android.R.drawable.btn_star_big_off)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText("주행 관찰중.." + getCurrent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build())


            sensorState = false


            registerReceiver(WalkingDetectReceiver(),IntentFilter().apply {
                addAction(TRANSITIONS_RECEIVER_ACTION)
            })

            registerReceiver(TransitionsReceiver(), filter)
            scheduleWalkingDetectWork()
            scheduleWalkingDetectWork2()
            scheduleWalkingDetectWork3()
            scheduleWalkingDetectWork4()
            scheduleWalkingDetectWork5()
        }

        return START_REDELIVER_INTENT
    }


    private fun scheduleWalkingDetectWork() {
        try {
            WorkManager.getInstance(this@BluetoothService).cancelUniqueWork("WalkingDetectWork")

            Handler(Looper.getMainLooper()).postDelayed({
                val workRequest = PeriodicWorkRequest.Builder(
                    WalkingDetectWorker::class.java,
                    15, TimeUnit.MINUTES
                ).build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "WalkingDetectWork",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            },1000)

        }catch (e:Exception){

        }
    }

    private fun scheduleWalkingDetectWork2() {
        try {
            WorkManager.getInstance(this@BluetoothService).cancelUniqueWork("WalkingDetectWorker2")

            Handler(Looper.getMainLooper()).postDelayed({
                val workRequest = PeriodicWorkRequest.Builder(
                    WalkingDetectWorker2::class.java,
                    15, TimeUnit.MINUTES
                ).setInitialDelay(3,TimeUnit.MINUTES).build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "WalkingDetectWorker2",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            },1000)

        }catch (e:Exception){

        }
    }


    private fun scheduleWalkingDetectWork3() {
        try {
            WorkManager.getInstance(this@BluetoothService).cancelUniqueWork("WalkingDetectWorker3")


            Handler(Looper.getMainLooper()).postDelayed({
                val workRequest = PeriodicWorkRequest.Builder(
                    WalkingDetectWorker3::class.java,
                    15, TimeUnit.MINUTES
                ).setInitialDelay(6,TimeUnit.MINUTES).build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "WalkingDetectWorker3",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            },1000)


        }catch (e:Exception){

        }
    }

    private fun scheduleWalkingDetectWork4() {
        try {
            WorkManager.getInstance(this@BluetoothService).cancelUniqueWork("WalkingDetectWorker4")

            Handler(Looper.getMainLooper()).postDelayed({
                val workRequest = PeriodicWorkRequest.Builder(
                    WalkingDetectWorker4::class.java,
                    15, TimeUnit.MINUTES
                ).setInitialDelay(9,TimeUnit.MINUTES).build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "WalkingDetectWorker4",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            },1000)

        }catch (e:Exception){

        }
    }

    private fun scheduleWalkingDetectWork5() {
        try {
            WorkManager.getInstance(this@BluetoothService).cancelUniqueWork("WalkingDetectWorker5")

            Handler(Looper.getMainLooper()).postDelayed({
                val workRequest = PeriodicWorkRequest.Builder(
                    WalkingDetectWorker5::class.java,
                    15, TimeUnit.MINUTES
                ).setInitialDelay(12,TimeUnit.MINUTES).build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "WalkingDetectWorker5",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            },1000)

        }catch (e:Exception){

        }
    }
    class WalkingDetectWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

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
            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_MUTABLE
            }
            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }


    class WalkingDetectWorker2(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

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
            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_MUTABLE
            }
            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }


    class WalkingDetectWorker3(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

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
            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_MUTABLE
            }
            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }
    class WalkingDetectWorker4(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

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
            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_MUTABLE
            }
            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }

    class WalkingDetectWorker5(context: Context, params: WorkerParameters) : Worker(context, params) {

        private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

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
            var flag = FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = FLAG_MUTABLE
            }
            val pendingIntent = getBroadcast(
                applicationContext,
                0,
                intent,
                flag
            )

            activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
        }
    }



    fun refreshNotiText(){
        if(sensorState)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 중..($distanceSum m) " + getCurrent()).build())
        else
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중....." + getCurrent()).build())
    }

    fun refreshNotiText(event:String){
        if(sensorState)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 중..($distanceSum m) " + event + ", " + getCurrent()).build())
        else
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중....." + event + ", " + getCurrent()).build())
    }


    inner class WalkingDetectReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (ActivityTransitionResult.hasResult(intent)) {
                refreshNotiText()
                val result = ActivityTransitionResult.extractResult(intent)
                result?.let {
                    for (event in it.transitionEvents) {
                        val activityType = event.activityType
                        val transitionType = event.transitionType

                        refreshNotiText(activityType.toString())

                        if(activityType == DetectedActivity.WALKING) {
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                // Walking 활동에 들어감
                                if(sensorState){
                                    scheduleWalkingDetectWork()
                                    scheduleWalkingDetectWork2()
                                    scheduleWalkingDetectWork3()
                                    scheduleWalkingDetectWork4()
                                    scheduleWalkingDetectWork5()

                                    if(maxDistance.max() < 300f) {
                                        if (pastMaxDistance.size != 0)
                                            stopSensor()
                                    }else{
                                        stopSensor()
                                    }

                                }
                            }
                        } else if(activityType == DetectedActivity.IN_VEHICLE){
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                // Vehicle 활동에 들어감
                                if(!sensorState){
                                    scheduleWalkingDetectWork()
                                    scheduleWalkingDetectWork2()
                                    scheduleWalkingDetectWork3()
                                    scheduleWalkingDetectWork4()
                                    scheduleWalkingDetectWork5()
                                }
                                startSensor(L1)
                            }
                        }
                    }
                }
            }
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
    fun startSensor(level:String){
        try {
            if (!sensorState) {
                /**
                 * W0D-74 1행 데이터 삭제
                 */
                firstLineState = true
                firstLineLocation = null

                sensorState = true
                PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
                driveDatabase = DriveDatabase.getDatabase(this)
                initDriveData(level)
                setLocation()

                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 중..($distanceSum m)").build())

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
                    sensorState = false
                    firstLineState = false
                    firstLineLocation = null
                    firstLocation = null
                    maxDistance = mutableListOf()
                    pastMaxDistance = mutableListOf()

                    makeSpeedInfo()
                    makeAccelerationInfo()
                    makePathLocationInfo()
                    makeDistanceBetween()
                    makeAltitudeFromGpsInfo()

                    writeToRoom()
                    callApi()

                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                    fusedLocationClient = null
                }
            }
        }catch (e:Exception){
        }
    }

    fun stopSensor(){
        try {
            if (sensorState) {
                sensorState = false
                firstLineState = false
                firstLineLocation = null
                firstLocation = null
                maxDistance = mutableListOf()
                pastMaxDistance = mutableListOf()

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()

                writeToRoom()
                callApi()

                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null

            }
        }catch(e:Exception){
        }
    }

    fun stopSensorNotSave(){
        try {
            if (sensorState) {
                sensorState = false
                firstLineState = false
                firstLineLocation = null
                firstLocation = null
                maxDistance = mutableListOf()
                pastMaxDistance = mutableListOf()

                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중.. NotSave" + getCurrent()).build())

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()

                fusedLocationClient?.removeLocationUpdates(locationCallback)
                fusedLocationClient = null

            }
        }catch(e:Exception){
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

        distance_array = MutableList(24) { 0f } // 23개 시간대의 distance
        sudden_deceleration_array = MutableList(24) { 0 } // 23개 시간대의 sudden_deceleration 갯수
        sudden_stop_array = MutableList(24) { 0 } // 23개 시간대의 sudden_stop 갯수
        sudden_acceleration_array = MutableList(24) { 0 }// 23개 시간대의 sudden_acceleration 갯수
        sudden_start_array = MutableList(24) { 0 }  // 23개 시간대의 sudden_start 갯수
        high_speed_driving_array = MutableList(24) { 0f } // 23개 시간대의 high_speed_driving 거리
        low_speed_driving_array = MutableList(24) { 0f } // 23개 시간대의 low_speed_driving 거리
        constant_speed_driving_array = MutableList(24) { 0f } // 23개 시간대의 constant_speed_driving 거리
        harsh_driving_array = MutableList(24) { 0f } // 23개 시간대의 harsh_driving 거리
        sumSuddenDecelerationDistance = 0f

        constantList1 = MutableList(24) {0f}
        constantList2 = MutableList(24) {0f}
        constantList3 = MutableList(24) {0f}
        constantList4 = MutableList(24) {0f}
        constantList5 = MutableList(24) {0f}
        firstConstantTimeStamp = 0L

        maxSpeed = 0f
        distanceSum = 0f
        startTimeStamp = System.currentTimeMillis()
        gpsInfo = mutableListOf()
        gpsInfoForApi = mutableListOf()
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


        driveDtoForApi = DriveDtoForApi(
            Build.MANUFACTURER,
            Build.VERSION.RELEASE,
            Build.MODEL,
            UUID.randomUUID().toString(),
            PreferenceUtil.getPref(this, PreferenceUtil.USER_NAME, "")!!,
            format.format(time).toString(),
            startTimeStamp,
            0L,
            level,
            true,
            gpsInfoForApi,
        )

    }

    inner class TransitionsReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
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

    private fun setLocation2() {
        // FusedLocationProviderClient 초기화
        fusedLocationClient2 = LocationServices.getFusedLocationProviderClient(this)


        // 위치 업데이트 요청 설정
        locationRequest2 = LocationRequest.create()
        locationRequest2.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest2.setInterval(INTERVAL2) // INTERVAL 마다 업데이트 요청


        // 위치 업데이트 리스너 생성
        locationCallback2 = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location = locationResult.lastLocation

                if(location.speed*MS_TO_KH > 30f){
                    if(!sensorState){
                        scheduleWalkingDetectWork()
                        scheduleWalkingDetectWork2()
                        scheduleWalkingDetectWork3()
                        scheduleWalkingDetectWork4()
                        scheduleWalkingDetectWork5()
                    }
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
                    }else{
                        firstLineState = false
                        firstLineLocation = locationResult.lastLocation
                        pastLocation = locationResult.lastLocation
                        pastTimeStamp = locationResult.lastLocation.time
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

        // 1717370784111
        // 1717370780111

        getAltitude(location)
        getPathLocation(location)
        getSpeed(location)

        var distance = 0f
        if(pastLocation != null){
            distance = pastLocation!!.distanceTo(location)
        }

        val speed = location.speed * MS_TO_KH
        val acceleration = (location.speed * MS_TO_KH) - (pastSpeed * MS_TO_KH)


        gpsInfo.add(EachGpsDto(timeStamp, location.latitude, location.longitude, String.format("%.2f",location.speed * MS_TO_KH).toFloat(),String.format("%.2f",distance).toFloat(),String.format("%.2f", location.altitude).toDouble(), String.format("%.2f",(location.speed * MS_TO_KH) - (pastSpeed * MS_TO_KH)).toFloat()))
        gpsInfoForApi.add(EachGpsDtoForApi(timeStamp, String.format("%.2f",speed).toFloat() ,String.format("%.2f",distance).toFloat(),String.format("%.2f", location.altitude).toDouble(), String.format("%.2f",acceleration).toFloat()))


        var HH = getDateFromTimeStampToHH(timeStamp)

        /**
         * 거리 계산
         */
        distance_array[HH] = distance_array[HH] + distance


        if(maxDistance.size > 1800){
            if (maxDistance.max() < 300f) {
                if(pastMaxDistance.size != 0)
                    stopSensor()
            }
            pastMaxDistance = maxDistance.toMutableList()
            maxDistance = mutableListOf()
            firstLocation = null
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
    }

    private fun makeAccelerationInfo() {
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
    }

    private fun makeDistanceBetween(){
    }

    private fun getAltitude(location: Location) {
        altitudeInfoFromGps =
            altitudeInfoFromGps + getCurrent() + "," + location.altitude + "\n"
    }

    private fun makeAltitudeFromGpsInfo() {
    }

    fun writeToRoom(){
        Executors.newSingleThreadExecutor().execute{
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
                    constant_speed_driving_array.toList(),
                    harsh_driving_array.toList(),
                    sumSuddenDecelerationDistance,
                    gpsInfo)

                driveDatabase?.driveDao()?.insert(drive)
            } catch (e:Exception){
            }
        }
    }

    private fun callApi(){
        driveDtoForApi.gpses = gpsInfoForApi
        driveDtoForApi.endTimestamp = System.currentTimeMillis()

        val driveForApi = DriveForApi(
            driveDtoForApi.tracking_id,
            driveDtoForApi.manufacturer,
            driveDtoForApi.version,
            driveDtoForApi.deviceModel,
            driveDtoForApi.deviceUuid,
            driveDtoForApi.username,
            driveDtoForApi.startTimeStamp,
            driveDtoForApi.endTimestamp,
            driveDtoForApi.verification,
            driveDtoForApi.automobile,
            driveDtoForApi.gpses
        )

        val gson = Gson()
        val jsonParam = gson.toJson(driveForApi)

        if (isInternetConnected(this@BluetoothService)) {
            apiService().postDrivingInfo(jsonParam).enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    writeToRoomForApi(driveForApi)
                }
            })

        } else {
            writeToRoomForApi(driveForApi)
        }

    }

    fun writeToRoomForApi(driveForApi:DriveForApi){
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

    private fun getDateFromTimeStampToHHMM(timeStamp:Long) : Int{
        val format = SimpleDateFormat("HHmm")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }


    private fun getDateFromTimeStampToSS(timeStamp:Long) : Int{
        val format = SimpleDateFormat("ss")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    private fun getDateFromTimeStampToMM(timeStamp:Long) : Int{
        val format = SimpleDateFormat("mm")
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return format.format(timeStamp).toInt()
    }

    fun apiService(): ApiServiceInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl("https://dev.charancha.com/").client(client)
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