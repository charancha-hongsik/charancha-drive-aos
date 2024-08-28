package com.milelog.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.milelog.BuildConfig
import com.milelog.MyApplication
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import com.milelog.retrofit.request.PatchDeviceInfoRequest
import com.milelog.retrofit.request.PostDeviceInfoRequest
import com.milelog.retrofit.response.PostConnectDeviceResponse
import com.milelog.room.database.DriveDatabase
import com.milelog.room.entity.AlarmEntity
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


class MyFirebaseMessagingService : FirebaseMessagingService() {
    // 메시지를 수신할 때 호출 된다.
    // 수신된 RemoteMessage 객체를 기준으로 작업을 수행하고 메시지 데이터를 가져올 수 있다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(PreferenceUtil.getPref(applicationContext, PreferenceUtil.REFRESH_TOKEN, "") != ""){
            // 메시지에 데이터 페이로드가 포함 되어 있는지 확인한다.
            // 페이로드란 전송된 데이터를 의미한다.
            if (remoteMessage.data.isNotEmpty()) {
                // deeplink 없으면 메인 화면

                if(remoteMessage.data["imageUrl"].toString().isNotEmpty()){
                    DriveDatabase.getDatabase(applicationContext).alarmDao().insert(
                        AlarmEntity(
                            user_id = PreferenceUtil.getPref(this, PreferenceUtil.USER_ID, "")!!,
                            title = remoteMessage.data["title"].toString(),
                            body = remoteMessage.data["body"].toString(),
                            deepLink = remoteMessage.data["deepLink"].toString(),
                            timestamp = remoteMessage.data["timestamp"].toString(),
                            imageUrl = remoteMessage.data["imageUrl"].toString(),
                            type = remoteMessage.data["type"].toString(),
                            isRequired = true
                        )
                    )

                    getBitmapFromGlide(
                        title = remoteMessage.data["title"].toString(),
                        body = remoteMessage.data["body"].toString(),
                        deepLink = remoteMessage.data["deepLink"].toString(),
                        timestamp = remoteMessage.data["timestamp"].toString(),
                        imageUrl = remoteMessage.data["imageUrl"].toString(),
                        type = remoteMessage.data["type"].toString()
                    )
                } else{
                    DriveDatabase.getDatabase(applicationContext).alarmDao().insert(
                        AlarmEntity(
                            user_id = PreferenceUtil.getPref(this, PreferenceUtil.USER_ID, "")!!,
                            title = remoteMessage.data["title"].toString(),
                            body = remoteMessage.data["body"].toString(),
                            deepLink = remoteMessage.data["deepLink"].toString(),
                            timestamp = remoteMessage.data["timestamp"].toString(),
                            imageUrl = remoteMessage.data["imageUrl"].toString(),
                            type = remoteMessage.data["type"].toString(),
                            isRequired = true
                        )
                    )

                    sendNotification(
                        title = remoteMessage.data["title"].toString(),
                        body = remoteMessage.data["body"].toString(),
                        deepLink = remoteMessage.data["deepLink"].toString(),
                        timestamp = remoteMessage.data["timestamp"].toString(),
                        img = null,
                        type = remoteMessage.data["type"].toString()
                    )
                }
            }
        }
    }

    fun showCustomToast(context: Context, message: String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_toast, null)

        // Set the text in the custom layout
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message

        // Create and display the toast
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }



    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 장시간 실행 (10초 이상) 작업의 경우 WorkManager를 사용하여 비동기 작업을 예약한다.
    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
    }

    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 10초 이내로 걸릴 때 메시지를 처리한다.
    private fun handleNow() {

    }

    private fun sendRegistrationToServer(token: String) {
        PreferenceUtil.getPref(applicationContext, PreferenceUtil.USER_ID, "")?.let{ userId->
            val patchDeviceInfoRequest = PatchDeviceInfoRequest(
                osVersion = Build.VERSION.SDK_INT.toString(),
                appVersion = BuildConfig.VERSION_NAME,
                fcmDeviceToken = token,
                userId = userId
            )

            val gson = Gson()
            val jsonParam = gson.toJson(patchDeviceInfoRequest)

            PreferenceUtil.getPref(applicationContext, PreferenceUtil.DEVICE_ID_FOR_FCM, "")?.let{ deviceId->
                    apiService().patchDeviceInfo(deviceId, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                        Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.code() == 200 || response.code() == 201){

                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                        }
                    })
                }
            }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    // 수신 된 FCM 메시지를 포함하는 간단한 알림을 만들고 표시한다.
    /**
     * // Uri 생성
    val uri: Uri = Uri.parse("splash://example")

    // URI의 스키마(scheme) 추출
    val scheme = uri.scheme // "splash"

    // URI의 호스트(host) 추출
    val host = uri.host // "example"

    // URI의 경로(path) 추출
    val path = uri.path // 경로가 없으므로 null이 반환됩니다

    // URI의 쿼리 파라미터(query parameters) 추출 (예를 들어, splash://example?key=value)
    val queryParams = uri.query // 쿼리 파라미터가 없으므로 null이 반환됩니다

    // URI의 특정 쿼리 파라미터 값 추출
    val specificParam = uri.getQueryParameter("key") // "key" 파라미터가 없으므로 null이 반환됩니다
     */
    private fun sendNotification(title:String, body:String, deepLink:String, timestamp:String, img: Bitmap?, type:String) {
        // Assign "splash://example" if deepLink is null
        var safeDeepLink = "milelog://splash"
        if(MyApplication.isInForeground){
            safeDeepLink = "milelog://alarm"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeDeepLink))
        intent.putExtra("deeplink",true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setNumber(0)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
//            .setStyle(NotificationCompat.BigPictureStyle()
//                .bigPicture(img))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        img?.let{
            notificationBuilder.setLargeIcon(img)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 이상에서 알림을 제공하려면 앱의 알림 채널을 시스템에 등록해야 한다.
        val channel = NotificationChannel(
            channelId,
            "일반 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.setShowBadge(false)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun getBitmapFromGlide(title:String, body:String, deepLink:String, timestamp:String, imageUrl: String, type:String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    sendNotification(
                        title = title,
                        body = body,
                        deepLink = deepLink,
                        timestamp = timestamp,
                        img = resource,
                        type = type
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.

                }
            })
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            return Result.success()
        }
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
}