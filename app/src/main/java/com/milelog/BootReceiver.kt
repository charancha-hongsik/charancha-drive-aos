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
}