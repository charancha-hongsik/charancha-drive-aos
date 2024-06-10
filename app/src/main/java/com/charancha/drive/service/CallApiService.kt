package com.charancha.drive.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.room.database.DriveDatabase
import com.charancha.drive.viewmodel.DetailDriveHistoryViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CallApiService: Service() {

    lateinit var notification: NotificationCompat.Builder
    val CHANNEL_ID = "my_channel_03"
    val channel = NotificationChannel(
        CHANNEL_ID,
        "call Api Service",
        NotificationManager.IMPORTANCE_DEFAULT
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setNotification()
        callApi()

        return super.onStartCommand(intent, flags, startId)
    }

    fun setNotification(){
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        notification = NotificationCompat.Builder(this, CHANNEL_ID)


        startForeground(2, notification.setSmallIcon(android.R.drawable.btn_star_big_off)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentText("데이터를 보내는 중입니다.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .build())



    }

    fun callApi(){
        if(isInternetConnected(this@CallApiService)){
            Thread {
                val driveDatabase: DriveDatabase = DriveDatabase.getDatabase(this@CallApiService)
                driveDatabase.driveForApiDao().allDriveLimit5?.let {
                    if (it.isNotEmpty()) {
                        for (drive in it) {
                            val gson = Gson()
                            val jsonParam = gson.toJson(drive)

                            apiService().postDrivingInfo(jsonParam).enqueue(object : Callback<JsonObject> {
                                override fun onResponse(
                                    call: Call<JsonObject>,
                                    response: Response<JsonObject>
                                ) {
                                    // 보낸 데이터 삭제
                                    driveDatabase.driveForApiDao().deleteByTrackingId(drive.tracking_id)


                                    if(drive.tracking_id == it.last().tracking_id){
                                        val intent = Intent(this@CallApiService, CallApiService::class.java)
                                        stopService(intent)
                                    }
                                }

                                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                    val intent = Intent(this@CallApiService, CallApiService::class.java)
                                    stopService(intent)
                                }
                            })
                        }
                    } else {
                        val intent = Intent(this@CallApiService, CallApiService::class.java)
                        stopService(intent)
                    }
                }
            }.start()

        }else{
            val intent = Intent(this@CallApiService, CallApiService::class.java)
            stopService(intent)
        }
    }

    // 인터넷 연결 상태를 확인하는 함수
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }



    fun apiService(): ApiServiceInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder().baseUrl("https://dev.charancha.com/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }

}