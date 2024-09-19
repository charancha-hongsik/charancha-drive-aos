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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.milelog.service.BluetoothService

class NotificationDeleteReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_RESTART_NOTIFICATION = "com.yourapp.ACTION_RESTART_NOTIFICATION"
    }    override fun onReceive(context: Context, intent: Intent) {
        // 여기에 알림이 삭제되었을 때의 작업을 작성합니다.

        val bluetoothIntent = Intent(context, BluetoothService::class.java)
        bluetoothIntent.action = ACTION_RESTART_NOTIFICATION
        context.startForegroundService(bluetoothIntent)
    }

    fun isMyServiceRunning(context:Context,serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}