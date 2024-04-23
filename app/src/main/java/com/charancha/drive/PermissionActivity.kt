package com.charancha.drive

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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
    }

    private lateinit var tvPermission: TextView
    private lateinit var btnPermission: Button
    private var permissionNo = PERMISSION_ACCESS_FINE_LOCATION


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        init()
    }

    private fun init(){
        setResources()
        setListener()
    }

    private fun setResources(){
        tvPermission = findViewById(R.id.tv_permission)
        btnPermission = findViewById(R.id.btn_permission)
    }

    private fun setListener(){
        btnPermission.setOnClickListener {
            if(btnPermission.text.contains("시작하기")){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else{
                when(permissionNo){
                    /**
                     * OS 28 이상
                     */
                    PERMISSION_ACCESS_FINE_LOCATION -> {
                        checkPermission(mutableListOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).toTypedArray(), PERMISSION_ACCESS_FINE_LOCATION)
                    }

                    /**
                     * OS 29 이상
                     */
                    PERMISSION_ACCESS_BACKGROUND_LOCATION -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            checkPermission(mutableListOf(ACCESS_BACKGROUND_LOCATION).toTypedArray(), PERMISSION_ACCESS_BACKGROUND_LOCATION)
                        }
                    }

                    /**
                     * OS 29 이상
                     */
                    PERMISSION_ACTIVITY_RECOGNITION -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            checkPermission(mutableListOf(ACTIVITY_RECOGNITION).toTypedArray(), PERMISSION_ACTIVITY_RECOGNITION)
                        }
                    }

                    /**
                     * OS 31 이상
                     */
                    PERMISSION_BLUETOOTH_CONNECT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            checkPermission(mutableListOf(BLUETOOTH_CONNECT).toTypedArray(), PERMISSION_BLUETOOTH_CONNECT)
                        }
                    }

                    /**
                     * OS 33 이상
                     */
                    PERMISSION_POST_NOTIFICATIONS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            checkPermission(mutableListOf(POST_NOTIFICATIONS).toTypedArray(), PERMISSION_POST_NOTIFICATIONS)
                        }
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_ACCESS_FINE_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        // OS 29 이상 -> PERMISSION_ACCESS_BACKGROUND_LOCATION 요청 UI로 수정
                        tvPermission.text = "PERMISSION_ACCESS_BACKGROUND_LOCATION"
                        permissionNo = PERMISSION_ACCESS_BACKGROUND_LOCATION
                    }else{
                        // OS 29 이상 -> PERMISSION_BLUETOOTH_CONNECT 요청 UI로 수정
                        tvPermission.text = "PERMISSION_ACTIVITY_RECOGNITION"
                        permissionNo = PERMISSION_ACTIVITY_RECOGNITION
                    }

                } else{
                    // OS 29 미만 -> 시작하기 UI로 수정
                    tvPermission.text = "시작하세요."
                    btnPermission.text = "시작하기"
                }
            }

            PERMISSION_ACCESS_BACKGROUND_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // OS 29 이상 -> PERMISSION_ACTIVITY_RECOGNITION 요청 UI로 수정
                    tvPermission.text = "PERMISSION_ACTIVITY_RECOGNITION"
                    permissionNo = PERMISSION_ACTIVITY_RECOGNITION
                } else{
                    // OS 29 미만 -> 시작하기 UI로 수정
                    tvPermission.text = "시작하세요."
                    btnPermission.text = "시작하기"
                }
            }

            PERMISSION_ACTIVITY_RECOGNITION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // OS 31 이상 -> PERMISSION_BLUETOOTH_CONNECT 요청 UI로 수정
                    tvPermission.text = "PERMISSION_BLUETOOTH_CONNECT"
                    permissionNo = PERMISSION_BLUETOOTH_CONNECT
                } else{
                    // OS 31 미만 -> 시작하기 UI로 수정
                    tvPermission.text = "시작하세요."
                    btnPermission.text = "시작하기"
                }
            }

            PERMISSION_BLUETOOTH_CONNECT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // OS 33 이상 -> PERMISSION_POST_NOTIFICATIONS 요청 UI로 수정
                    tvPermission.text = "PERMISSION_POST_NOTIFICATIONS"
                    permissionNo = PERMISSION_POST_NOTIFICATIONS
                } else{
                    // OS 33 미만 -> 시작하기 UI로 수정
                    tvPermission.text = "시작하세요."
                    btnPermission.text = "시작하기"
                }
            }

            PERMISSION_POST_NOTIFICATIONS -> {
                // 시작하기 UI로 수정
                tvPermission.text = "시작하세요."
                btnPermission.text = "시작하기"
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }
}