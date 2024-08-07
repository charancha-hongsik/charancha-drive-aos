package com.milelog.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.milelog.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 메시지를 수신할 때 호출 된다.
    // 수신된 RemoteMessage 객체를 기준으로 작업을 수행하고 메시지 데이터를 가져올 수 있다.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        // 메시지에 데이터 페이로드가 포함 되어 있는지 확인한다.
        // 페이로드란 전송된 데이터를 의미한다.
        if (remoteMessage.data.isNotEmpty()) {
            // deeplink 없으면 메인 화면
            sendNotification(
                remoteMessage.data["title"].toString(),
                remoteMessage.data["body"].toString(),
                remoteMessage.data["deepLink"].toString()
            )


        } else {
            // 메시지에 알림 페이로드가 포함되어 있는지 확인한다.
            remoteMessage.notification?.let {
                sendNotification(
                    remoteMessage.notification!!.title.toString(),
                    remoteMessage.notification!!.body.toString(),
                    null
                    )
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

    // 타사 서버에 토큰을 유지해주는 메서드이다.
    private fun sendRegistrationToServer(token: String?) {

    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    // 수신 된 FCM 메시지를 포함하는 간단한 알림을 만들고 표시한다.
    private fun sendNotification(title: String, body: String, deepLink:String?) {
        // Assign "splash://example" if deepLink is null
        var safeDeepLink = "splash://example"

        if(!deepLink.isNullOrBlank()){
            safeDeepLink = deepLink
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeDeepLink))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 이상에서 알림을 제공하려면 앱의 알림 채널을 시스템에 등록해야 한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
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
}