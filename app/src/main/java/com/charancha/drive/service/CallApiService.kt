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
        if(isInternetConnected(this@CallApiService)){
            apiService().sections().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.d("testsetset","testsetsetestestse onResponse")
                    val intent = Intent(this@CallApiService, CallApiService::class.java)
                    stopService(intent)
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("testsetset","testsetsetestestse onFailure")
                    val intent = Intent(this@CallApiService, CallApiService::class.java)
                    stopService(intent)
                }
            })
        }else{

        }

//        Log.d("testestest","testestsetes device model name :: " + Build.MODEL)
//        Log.d("testestest","testestsetes os version :: " + Build.VERSION.RELEASE)
//        Log.d("testestest","testestsetes manufacturer name :: " + Build.MANUFACTURER)
//        Log.d("testestest","testestsetes uuid  :: " + UUID.randomUUID().toString())



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