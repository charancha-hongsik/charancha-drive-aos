package com.milelog

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import com.milelog.retrofit.request.PatchDeviceInfoRequest
import com.milelog.retrofit.request.PostDeviceInfoRequest
import com.milelog.retrofit.response.PostConnectDeviceResponse
import com.milelog.service.BluetoothService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // This is where you handle the reboot event
            Log.d("testestestest","testestestset :: BootReceiver onReceive")

            // You can start a service, schedule a job, etc. here
            if(ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(context, ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                        if(!isMyServiceRunning(context,BluetoothService::class.java)){
                            val bluetoothIntent = Intent(context, BluetoothService::class.java)
                            context.startForegroundService(bluetoothIntent)
                        }
                    }
                }else{
                    if(!isMyServiceRunning(context,BluetoothService::class.java)){
                        val bluetoothIntent = Intent(context, BluetoothService::class.java)
                        context.startForegroundService(bluetoothIntent)
                    }
                }
            }

            setFcmToken(context)
        }
    }

    private fun isMyServiceRunning(context:Context,serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun setFcmToken(context:Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            try {
                if (task.isComplete) {
                    val token = task.result
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(
                        {
                            Log.d("tetsetsetset","testsetsetes token2 :: " + token)
                            if(!PreferenceUtil.getBooleanPref(context, PreferenceUtil.POST_DEVICE_INFO_STATE, false)){
                                val postDeviceInfoRequest = PostDeviceInfoRequest(
                                    manufacturer = Build.MANUFACTURER,
                                    model = Build.MODEL,
                                    os = "AOS",
                                    osVersion = Build.VERSION.SDK_INT.toString(),
                                    deviceType = "PHONE",
                                    appVersion = BuildConfig.VERSION_NAME,
                                    fcmDeviceToken = token
                                )

                                val gson = Gson()
                                val jsonParam = gson.toJson(postDeviceInfoRequest)

                                apiService(context).postDeviceInfo(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                                    Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        if (response.code() == 200 || response.code() == 201){
                                            val postConnectDeviceResponse = gson.fromJson(response.body()?.string(), PostConnectDeviceResponse::class.java)

                                            PreferenceUtil.putBooleanPref(context, PreferenceUtil.POST_DEVICE_INFO_STATE, true)
                                            PreferenceUtil.putPref(context, PreferenceUtil.DEVICE_ID_FOR_FCM, postConnectDeviceResponse.id)
                                        }

                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                                    }
                                })
                            }else{
                                sendRegistrationToServer(context, token)
                            }
                        },
                        5000
                    )
                }
            } catch (e: java.lang.Exception) {
            }
        }
    }

    private fun sendRegistrationToServer(context:Context,token: String) {
        PreferenceUtil.getPref(context, PreferenceUtil.USER_ID, "")?.let{ userId->
            val patchDeviceInfoRequest = PatchDeviceInfoRequest(
                osVersion = Build.VERSION.SDK_INT.toString(),
                appVersion = BuildConfig.VERSION_NAME,
                fcmDeviceToken = token,
                userId = userId
            )

            val gson = Gson()
            val jsonParam = gson.toJson(patchDeviceInfoRequest)

            PreferenceUtil.getPref(context, PreferenceUtil.DEVICE_ID_FOR_FCM, "")?.let{ deviceId->
                apiService(context).patchDeviceInfo(deviceId, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.code() == 200 || response.code() == 201){
                            Log.d("testsetestest","testestestest :: " + response.code())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    }
                })
            }
        }
    }


    fun apiService(context:Context, readTimeOut:Long = 30): ApiServiceInterface {


        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_API_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }
}

