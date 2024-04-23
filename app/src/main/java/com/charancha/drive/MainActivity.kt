package com.charancha.drive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                /**
                 * OS 33 이상
                 */
                mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.POST_NOTIFICATIONS
                ).apply {

                }.toTypedArray()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                /**
                 * OS 31, 32
                 */
                mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray()
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /**
                 * OS 29, 30
                 */
                mutableListOf (
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else {
                /**
                 * OS 28
                 */
                mutableListOf (
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).apply {

                }.toTypedArray()
            }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}