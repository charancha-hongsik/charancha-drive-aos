package com.charancha.drive.activity

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.charancha.drive.R

class PermissionActivity: AppCompatActivity(){
    companion object {
        private const val PERMISSION_ACCESS_FINE_LOCATION = 1000
        private const val PERMISSION_ACCESS_BACKGROUND_LOCATION = 1001
        private const val PERMISSION_ACTIVITY_RECOGNITION = 1002

        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                ).apply {

                }.toTypedArray()
            }
    }

    private lateinit var tvPermission: TextView
    private lateinit var btnPermission: ConstraintLayout
    private lateinit var tvNext:TextView
    private lateinit var ivPermission: ImageView
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
        btnPermission = findViewById(R.id.layout_next)
        tvNext = findViewById(R.id.tv_next)
        ivPermission = findViewById(R.id.iv_permission)
    }

    private fun setListener(){
        btnPermission.setOnClickListener {
            if(tvNext.text.contains("시작하기")){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else{
                when(permissionNo){
                    /**
                     * OS 28 이상
                     */
                    PERMISSION_ACCESS_FINE_LOCATION -> {
                        if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            checkPermission(mutableListOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).toTypedArray(), PERMISSION_ACCESS_FINE_LOCATION)
                        }else{
                            setNextOfFineLocationPermission()
                        }
                    }

                    /**
                     * OS 29 이상
                     */
                    PERMISSION_ACCESS_BACKGROUND_LOCATION -> {
                        if(ContextCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                checkPermission(mutableListOf(ACCESS_BACKGROUND_LOCATION).toTypedArray(), PERMISSION_ACCESS_BACKGROUND_LOCATION)
                            }
                        }else{
                            setNextOfBackgroundLocationPermission()
                        }
                    }

                    /**
                     * OS 29 이상
                     */
                    PERMISSION_ACTIVITY_RECOGNITION -> {
                        if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                checkPermission(mutableListOf(ACTIVITY_RECOGNITION).toTypedArray(), PERMISSION_ACTIVITY_RECOGNITION)
                            }
                        }else{
                            setNextOfActivityRecognition()
                        }
                    }
                }
            }
        }
    }

    private fun setNextOfFineLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                // OS 29 이상 -> PERMISSION_ACCESS_BACKGROUND_LOCATION 요청 UI로 수정
                ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_background_location))
                permissionNo = PERMISSION_ACCESS_BACKGROUND_LOCATION
            }else{
                // OS 29 이상 -> PERMISSION_ACTIVITY_RECOGNITION 요청 UI로 수정
                ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_useractivity))
                permissionNo = PERMISSION_ACTIVITY_RECOGNITION
            }

        } else{
            goToOnboardingActivity()
        }
    }

    private fun setNextOfBackgroundLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // OS 29 이상 -> PERMISSION_ACTIVITY_RECOGNITION 요청 UI로 수정
            ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_useractivity))
            permissionNo = PERMISSION_ACTIVITY_RECOGNITION
        } else{
            goToOnboardingActivity()
        }
    }

    private fun setNextOfActivityRecognition(){
        goToOnboardingActivity()
    }

    private fun goToOnboardingActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_ACCESS_FINE_LOCATION -> { setNextOfFineLocationPermission() }

            PERMISSION_ACCESS_BACKGROUND_LOCATION -> { setNextOfBackgroundLocationPermission() }

            PERMISSION_ACTIVITY_RECOGNITION -> { setNextOfActivityRecognition() }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}