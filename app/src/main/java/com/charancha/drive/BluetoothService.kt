package com.charancha.drive

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO
import android.bluetooth.BluetoothClass.Service.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*


/**
 * The Service will only run in one instance. However, everytime you start the service, the onStartCommand() method is called.
 */
class BluetoothService : Service() {
    companion object {
        const val TRANSITIONS_RECEIVER_ACTION = "TRANSITIONS_RECEIVER_ACTION"

        const val TAG = "AutoConnectionDetector"

        const val HANDS_FREE = "240408"

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

        private const val QUERY_TOKEN = 42

        private const val CAR_CONNECTION_AUTHORITY = "androidx.car.app.connection"

        private val PROJECTION_HOST_URI = Uri.Builder().scheme("content").authority(CAR_CONNECTION_AUTHORITY).build()
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

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerActivityTransitionUpdates()
        registerReceiver(transitionReceiver, filter)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        carConnectionQueryHandler = CarConnectionQueryHandler(contentResolver)

        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_02"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "check blueToothConnect",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }

        request = ActivityTransitionRequest(transitions)
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE)
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
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")

        // query for the file
        val cursor: Cursor? = contentResolver.query(
            extVolumeUri,
            arrayOf(MediaStore.Downloads.DISPLAY_NAME,MediaStore.Downloads._ID),
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

        if(fileUri == null){
            fileUri = contentResolver.insert(extVolumeUri, contentValues)
        }

        if (fileUri != null) {
            val os = contentResolver.openOutputStream(fileUri, "wa")

            if (os != null) {
                os.write(content.toByteArray())
                os.close()
            }
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
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportToFile("CONNECTION_TYPE_CONNECTED",getCurrent()+"\n\n")
                }else{
                    generateNoteOnSD("CONNECTION_TYPE_CONNECTED",getCurrent()+"\n\n")
                }

            }
        }
    }

    private fun startSensorService(){
        val intent = Intent(this@BluetoothService, SensorService::class.java)
        startForegroundService(intent)
    }

    private fun stopSensorService(){
        stopService(Intent(this@BluetoothService, SensorService::class.java))
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
                                generateNoteOnSD("IN_VEHICLE ENTER " + getCurrent(),getCurrent()+"\n\n")
                            }
                        } else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("IN_VEHICLE EXIT",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("IN_VEHICLE EXIT " + getCurrent(),getCurrent()+"\n\n")
                            }
                        }
                    }
                }
            } else if(intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED){
                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                pairedDevices?.forEach { device ->
                    Log.d("testsetestestset","testsetsetset device name :: " + device.name)
                    Log.d("testsetestestset","testsetsetset device CoD :: " + device.bluetoothClass.toString())

                    if(device.bluetoothClass.toString() == HANDS_FREE){
                        if(isConnected(device)){
                            startSensorService()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("Bluetooth AUDIO_VIDEO_CAR_AUDIO CONNECTED",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("Bluetooth AUDIO_VIDEO_CAR_AUDIO CONNECTED" + getCurrent(),getCurrent()+"\n\n")
                            }
                        }
                    }
                }

            } else if(intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED){
                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->

                    Log.d("testsetestestset","testsetsetset device name :: " + device.name)
                    Log.d("testsetestestset","testsetsetset device CoD :: " + device.bluetoothClass.toString())


                    if(device.bluetoothClass.toString() == HANDS_FREE){
                        if(isConnected(device)){
                            stopSensorService()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("Bluetooth AUDIO_VIDEO_CAR_AUDIO DISCONNECTED",getCurrent()+"\n\n")
                            }else{
                                generateNoteOnSD("Bluetooth AUDIO_VIDEO_CAR_AUDIO DISCONNECTED" + getCurrent(),getCurrent()+"\n\n")
                            }
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
            val gpxfile = File(root, "$sFileName " + getCurrent() + ".txt")
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
}