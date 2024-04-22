package com.charancha.drive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionActivity: AppCompatActivity(){
    companion object {
        private const val PERMISSION_ACCESS_FINE_LOCATION = 1000
        private const val PERMISSION_ACCESS_BACKGROUND_LOCATION = 1001
        private const val PERMISSION_ACTIVITY_RECOGNITION = 1002
        private const val PERMISSION_BLUETOOTH_CONNECT = 1003
        private const val PERMISSION_POST_NOTIFICATIONS = 1004

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){

            /**
             * OS 28 이상
             */
            PERMISSION_ACCESS_FINE_LOCATION -> {
                // OS 28 -> 메인 화면으로 이동
                // OS 29 이상 -> PERMISSION_ACCESS_BACKGROUND_LOCATION 요청
            }

            /**
             * OS 29 이상
             */
            PERMISSION_ACCESS_BACKGROUND_LOCATION -> {
                // OS 29 미만 -> 메인 화면으로 이동
                // OS 29 이상 -> PERMISSION_ACTIVITY_RECOGNITION 요청
            }

            /**
             * OS 29 이상
             */
            PERMISSION_ACTIVITY_RECOGNITION -> {
                // OS 31 미만 -> 메인 화면으로 이동
                // OS 31 이상 -> PERMISSION_BLUETOOTH_CONNECT 요청
            }

            /**
             * OS 31 이상
             */
            PERMISSION_BLUETOOTH_CONNECT -> {
                // OS 33 미만 -> 메인 화면으로 이동
                // OS 33 이상 -> PERMISSION_POST_NOTIFICATIONS 요청
            }

            /**
             * OS 33 이상
             */
            PERMISSION_POST_NOTIFICATIONS -> {
                // 메인화면으로 이동
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkPermission(permission: String, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, mutableListOf(permission).toTypedArray(),code)
            return
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}