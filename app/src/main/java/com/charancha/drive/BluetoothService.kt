package com.charancha.drive

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_MUTABLE
import android.bluetooth.*
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO
import android.content.*
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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


    fun sendNotification(no:Int,contents:String) {

        val CHANNEL_ID = "my_channel_01"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "check blueToothConnect",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            val builder = NotificationCompat.Builder(this,"bluetooth")
            builder.setSmallIcon(android.R.drawable.ic_dialog_alert)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("activityType",no)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_MUTABLE)
            builder.setChannelId(CHANNEL_ID)
            builder.setContentIntent(pendingIntent)
            builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            builder.setContentTitle("Notifications Title")
            builder.setContentText(contents)
            builder.setSubText("Tap to view the website.")
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Will display the notification in the notification bar
            notificationManager.notify(2, builder.build())

        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }

    private fun registerActivityTransitionUpdates() {
        ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)
            .addOnSuccessListener {

            }.addOnFailureListener { e ->

            }
    }

    private fun unregisterActivityTransitionUpdates() {
        ActivityRecognition.getClient(this)
            .removeActivityTransitionUpdates(pendingIntent)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

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
                Log.d(TAG, "testsetsetsetse Null response from content provider when checking connection to the car, treating as disconnected")
                return
            }
            val carConnectionTypeColumn = response.getColumnIndex(CAR_CONNECTION_STATE)
            if (carConnectionTypeColumn < 0) {
                Log.d(TAG, "testsetsetsetse Connection to car response is missing the connection type, treating as disconnected")
                return
            }
            if (!response.moveToNext()) {
                Log.d(TAG, "testsetsetsetse Connection to car response is empty, treating as disconnected")
                return
            }
            val connectionState = response.getInt(carConnectionTypeColumn)
            if (connectionState == CONNECTION_TYPE_NOT_CONNECTED) {

                Log.d(TAG, "testsetsetsetse CONNECTION_TYPE_NOT_CONNECTED")


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportToFile("CONNECTION_TYPE_NOT_CONNECTED",getCurrent()+"\n\n")
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportToFile("CONNECTION_TYPE_CONNECTED",getCurrent()+"\n\n")
                }

                Log.d(TAG, "testsetsetsetse CONNECTION_TYPE_NOT_CONNECTED")

            }
        }
    }


    inner class TransitionsReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {

            if (ActivityTransitionResult.hasResult(intent)) {
                val result: ActivityTransitionResult = ActivityTransitionResult.extractResult(intent) ?: return
                for (event in result.transitionEvents) {
                    if(event.activityType == DetectedActivity.IN_VEHICLE){
                        if(event.transitionType.equals(ACTIVITY_TRANSITION_ENTER)){
//                            sendNotification(DetectedActivity.IN_VEHICLE, "IN_VEHICLE... ")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("IN_VEHICLE ENTER",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("IN_VEHICLE ENTER " + getCurrent(),getCurrent()+"\n\n")
                            }

                            var intent = Intent(this@BluetoothService, SensorService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else{
                                startService(intent)
                            }
                        } else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("IN_VEHICLE EXIT",getCurrent()+"\n\n")
                            } else{
                                generateNoteOnSD("IN_VEHICLE EXIT " + getCurrent(),getCurrent()+"\n\n")
                            }
                            stopService(Intent(this@BluetoothService, SensorService::class.java))
                        }
                    }
                }
            } else if(intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED){
                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->
                        if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_CAR_AUDIO){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportToFile("Bluetooth AUDIO_VIDEO_CAR_AUDIO CONNECTED",getCurrent()+"\n\n")
                            }
                        } else {

                        }
                }
            } else if(intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED){
                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->
                    if(device.bluetoothClass.deviceClass == AUDIO_VIDEO_CAR_AUDIO){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            exportToFile("Bluetooth AUDIO_VIDEO_CAR_AUDIO DISCONNECTED",getCurrent()+"\n\n")
                        }
                    } else {

                    }
                }
            } else{
//                queryForState()
            }
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