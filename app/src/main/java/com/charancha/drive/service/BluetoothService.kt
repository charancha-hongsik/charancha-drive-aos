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
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.calculateData
import com.charancha.drive.room.DriveDto
import com.charancha.drive.room.EachGpsDto
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.room.entity.Drive
import com.google.android.gms.location.*
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer


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

    private lateinit var sensorManager: SensorManager
    private var fusedLocationClient :FusedLocationProviderClient? = null
    // 위치 업데이트 요청 설정
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // 위치 업데이트 요청 설정
    private var sensorEventListener: SensorEventListener? = null
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


    /**
     *  타이머 시간동안 최대 반경을 구하기 위한 변수들
     */
    lateinit var distanceSumForAnHourTimer:Timer

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

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

        return START_REDELIVER_INTENT
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

    private fun scheduleWalkingDetectWork2() {
        try {
            val workRequest = PeriodicWorkRequest.Builder(
                WalkingDetectWorker2::class.java,
                15, TimeUnit.MINUTES
            ).setInitialDelay(3,TimeUnit.MINUTES).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WalkingDetectWorker2",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }catch (e:Exception){

        }
    }


    private fun scheduleWalkingDetectWork3() {
        try {
            val workRequest = PeriodicWorkRequest.Builder(
                WalkingDetectWorker3::class.java,
                15, TimeUnit.MINUTES
            ).setInitialDelay(6,TimeUnit.MINUTES).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WalkingDetectWorker3",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }catch (e:Exception){

        }
    }

    private fun scheduleWalkingDetectWork4() {
        try {
            val workRequest = PeriodicWorkRequest.Builder(
                WalkingDetectWorker4::class.java,
                15, TimeUnit.MINUTES
            ).setInitialDelay(9,TimeUnit.MINUTES).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WalkingDetectWorker4",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }catch (e:Exception){

        }
    }

    private fun scheduleWalkingDetectWork5() {
        try {
            val workRequest = PeriodicWorkRequest.Builder(
                WalkingDetectWorker5::class.java,
                15, TimeUnit.MINUTES
            ).setInitialDelay(12,TimeUnit.MINUTES).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WalkingDetectWorker5",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
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

                        if (activityType == DetectedActivity.WALKING) {
                            if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                // Walking 활동에 들어감
                                scheduleWalkingDetectWork()
                                scheduleWalkingDetectWork2()
                                scheduleWalkingDetectWork3()
                                scheduleWalkingDetectWork4()
                                scheduleWalkingDetectWork5()

                                stopSensor()
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

                startDistanceTimer()

                sensorState = true
                PreferenceUtil.putPref(this, PreferenceUtil.RUNNING_LEVEL, level)
                driveDatabase = DriveDatabase.getDatabase(this)
                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
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
                    firstLocation = null
                    maxDistance = 0f

                    stopDistanceTimer()

                    makeSpeedInfo()
                    makeAccelerationInfo()
                    makePathLocationInfo()
                    makeDistanceBetween()
                    makeAltitudeFromGpsInfo()

                    writeToRoom()

                    sensorManager.unregisterListener(sensorEventListener)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                    fusedLocationClient = null
                }
            }
        }catch (e:Exception){
        }
    }

    private fun startDistanceTimer(){
        distanceSumForAnHourTimer = timer(period = 3600000, initialDelay = 3600000) {
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

    fun stopSensor(){
        try {
            if (sensorState) {
                sensorState = false
                firstLineState = false
                firstLocation = null
                maxDistance = 0f

                stopDistanceTimer()

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()

                writeToRoom()

                sensorManager.unregisterListener(sensorEventListener)
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
                firstLocation = null
                maxDistance = 0f

                stopDistanceTimer()

                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification.setContentText("주행 관찰중.. NotSave" + getCurrent()).build())

                makeSpeedInfo()
                makeAccelerationInfo()
                makePathLocationInfo()
                makeDistanceBetween()
                makeAltitudeFromGpsInfo()

                sensorManager.unregisterListener(sensorEventListener)
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

                    }else{
                        firstLineState = false
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
        if(location.distanceTo(firstLocation!!) > maxDistance){
            maxDistance = location.distanceTo(firstLocation!!)
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

        gpsInfo.add(EachGpsDto(timeStamp, location.latitude, location.longitude, location.speed,distance,location.altitude, (location.speed) - (pastSpeed)))

        var HH = getDateFromTimeStampToHH(timeStamp)

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

        /**
         * 항속 주행거리 계산
         */
        if(firstConstantTimeStamp == 0L){
            // 첫 3분 시작 시점
            firstConstantTimeStamp = timeStamp
            if (location.speed * MS_TO_KH in 61f..80f) {
                constantList1[getDateFromTimeStampToHH(timeStamp)] = constantList1[getDateFromTimeStampToHH(
                    timeStamp
                )] + distance
            } else if(location.speed * MS_TO_KH in 81f..100f){
                constantList2[getDateFromTimeStampToHH(timeStamp)] = constantList1[getDateFromTimeStampToHH(
                    timeStamp
                )] + distance
            } else if(location.speed * MS_TO_KH in 101f..120f){
                constantList3[getDateFromTimeStampToHH(timeStamp)] = constantList1[getDateFromTimeStampToHH(
                    timeStamp
                )] + distance
            } else if(location.speed * MS_TO_KH in 121f..140f){
                constantList4[getDateFromTimeStampToHH(timeStamp)] = constantList1[getDateFromTimeStampToHH(
                    timeStamp
                )] + distance
            } else{
                constantList5[getDateFromTimeStampToHH(timeStamp)] =
                    constantList5[getDateFromTimeStampToHH(timeStamp)] + distance
            }


        } else if(timeStamp - firstConstantTimeStamp >= 180000){
            var countSum = constantList1.sum() + constantList2.sum() + constantList3.sum() + constantList4.sum() + constantList5.sum()
            // n번째 3분 시작 시점

            if(constantList1.sum()/countSum >= 0.8f || constantList2.sum()/countSum >= 0.8f || constantList3.sum()/countSum >= 0.8f || constantList4.sum()/countSum >= 0.8f){
                for(i: Int in 0..23){
                    constant_speed_driving_array[i] = constant_speed_driving_array[i] + constantList1[i] + constantList2[i] + constantList3[i] + constantList4[i]
                }
            }

            firstConstantTimeStamp = timeStamp
            constantList1 = MutableList(24) {0f}
            constantList2 = MutableList(24) {0f}
            constantList3 = MutableList(24) {0f}
            constantList4 = MutableList(24) {0f}
            constantList5 = MutableList(24) {0f}

            // 3분 간격 동안 계속 쌓기
            if (location.speed * MS_TO_KH in 61f..80f) {
                constantList1[getDateFromTimeStampToHH(timeStamp)] =
                    constantList1[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 81f..100f) {
                constantList2[getDateFromTimeStampToHH(timeStamp)] =
                    constantList3[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 101f..120f) {
                constantList3[getDateFromTimeStampToHH(timeStamp)] =
                    constantList4[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 121f..140f) {
                constantList4[getDateFromTimeStampToHH(timeStamp)] =
                    constantList4[getDateFromTimeStampToHH(timeStamp)] + distance
            } else{
                constantList5[getDateFromTimeStampToHH(timeStamp)] =
                    constantList5[getDateFromTimeStampToHH(timeStamp)] + distance
            }


        } else {
            // 3분 간격 동안 계속 쌓기
            if (location.speed * MS_TO_KH in 61f..80f) {
                constantList1[getDateFromTimeStampToHH(timeStamp)] =
                    constantList1[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 81f..100f) {
                constantList2[getDateFromTimeStampToHH(timeStamp)] =
                    constantList3[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 101f..120f) {
                constantList3[getDateFromTimeStampToHH(timeStamp)] =
                    constantList4[getDateFromTimeStampToHH(timeStamp)] + distance
            } else if (location.speed * MS_TO_KH in 121f..140f) {
                constantList4[getDateFromTimeStampToHH(timeStamp)] =
                    constantList4[getDateFromTimeStampToHH(timeStamp)] + distance
            } else{
                constantList5[getDateFromTimeStampToHH(timeStamp)] =
                    constantList5[getDateFromTimeStampToHH(timeStamp)] + distance
            }
        }

        if(firstLocation != null) {
            // firstLocation 23시
            // location 0시
            if(getDateFromTimeStampToHH(firstLocation!!.time) == 23 && getDateFromTimeStampToHH(location.time) == 0){
                maxDistance = 0f
                firstLocation = null
            } else{
                if ((getDateFromTimeStampToHHMM(location.time) - getDateFromTimeStampToHHMM(firstLocation!!.time)) == 100
                    && (getDateFromTimeStampToSS(location.time) == getDateFromTimeStampToSS(firstLocation!!.time))) {
                    if (maxDistance < 300f) {
                        stopSensorNotSave()
                    }

                    maxDistance = 0f
                    firstLocation = null
                } else if((getDateFromTimeStampToHHMM(location.time) - getDateFromTimeStampToHHMM(firstLocation!!.time)) != 0
                    && (getDateFromTimeStampToHHMM(location.time) - getDateFromTimeStampToHHMM(firstLocation!!.time)) % 100 == 0
                    && (getDateFromTimeStampToSS(location.time) == getDateFromTimeStampToSS(firstLocation!!.time))) {
                    if (maxDistance < 300f) {
                        stopSensor()
                    }

                    maxDistance = 0f
                    firstLocation = null
                }
            }
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
                    constant_speed_driving_array.toList(),
                    harsh_driving_array.toList(),
                    sumSuddenDecelerationDistance,
                    gpsInfo)

                driveDatabase?.driveDao()?.insert(drive)
            } catch (e:Exception){
            }
        }.start()
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
}