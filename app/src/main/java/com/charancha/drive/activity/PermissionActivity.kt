package com.charancha.drive.activity

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.PreferenceUtil.PERMISSION_ACCESS_BACKGROUND_LOCATION_THREE_TIMES
import com.charancha.drive.PreferenceUtil.PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES
import com.charancha.drive.PreferenceUtil.PERMISSION_ACTIVITY_RECOGNITION_THREE_TIMES
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

    private lateinit var btnPermission: ConstraintLayout
    private lateinit var tvNext:TextView
    private lateinit var ivPermission: ImageView
    private var permissionNo = PERMISSION_ACCESS_FINE_LOCATION

    private var settingState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)


        if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
            goToOnboardingActivity()
        } else{
            init()
        }
    }

    private fun init(){
        setResources()
        setListener()

        if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            setNextOfFineLocationPermission()
        }
    }

    private fun setResources(){
        btnPermission = findViewById(R.id.layout_next)
        tvNext = findViewById(R.id.tv_next)
        ivPermission = findViewById(R.id.iv_permission)
    }

    private fun setListener(){
        btnPermission.setOnClickListener {
            when(permissionNo){
                /**
                 * OS 28 이상
                 */
                PERMISSION_ACCESS_FINE_LOCATION -> {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            setNextOfFineLocationPermission()
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            ACCESS_FINE_LOCATION
                        ) -> {
                            checkPermission(mutableListOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).toTypedArray(), PERMISSION_ACCESS_FINE_LOCATION)
                            PreferenceUtil.putBooleanPref(this@PermissionActivity, PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES, true)
                        }

                        else -> {
                            if(!PreferenceUtil.getBooleanPref(this, PERMISSION_ACCESS_FINE_LOCATION_THREE_TIMES, false)){
                                checkPermission(mutableListOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).toTypedArray(), PERMISSION_ACCESS_FINE_LOCATION)
                            }else{
                                val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    val uri: Uri = Uri.fromParts("package", packageName, null)
                                    data = uri
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(openSettingsIntent)
                                setNextOfFineLocationPermission()
                            }
                        }
                    }
                }

                /**
                 * OS 29 이상
                 */
                PERMISSION_ACCESS_BACKGROUND_LOCATION -> {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            setNextOfBackgroundLocationPermission()
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            ACCESS_BACKGROUND_LOCATION
                        ) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                checkPermission(mutableListOf(ACCESS_BACKGROUND_LOCATION).toTypedArray(), PERMISSION_ACCESS_BACKGROUND_LOCATION)
                                PreferenceUtil.putBooleanPref(this@PermissionActivity, PERMISSION_ACCESS_BACKGROUND_LOCATION_THREE_TIMES, true)

                            }
                        }

                        else -> {
                            if(!PreferenceUtil.getBooleanPref(this, PERMISSION_ACCESS_BACKGROUND_LOCATION_THREE_TIMES, false)){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    checkPermission(mutableListOf(ACCESS_BACKGROUND_LOCATION).toTypedArray(), PERMISSION_ACCESS_BACKGROUND_LOCATION)
                                }
                            }else{
                                val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    val uri: Uri = Uri.fromParts("package", packageName, null)
                                    data = uri
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(openSettingsIntent)
                                setNextOfBackgroundLocationPermission()
                            }

                        }
                    }
                }

                /**
                 * OS 29 이상
                 */
                PERMISSION_ACTIVITY_RECOGNITION -> {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            ACTIVITY_RECOGNITION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            setNextOfActivityRecognition()
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            ACTIVITY_RECOGNITION
                        ) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                checkPermission(mutableListOf(ACTIVITY_RECOGNITION).toTypedArray(), PERMISSION_ACTIVITY_RECOGNITION)
                                PreferenceUtil.putBooleanPref(this@PermissionActivity, PERMISSION_ACTIVITY_RECOGNITION_THREE_TIMES, true)

                            }
                        }

                        else -> {
                            if(!PreferenceUtil.getBooleanPref(this, PERMISSION_ACTIVITY_RECOGNITION_THREE_TIMES, false)){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    checkPermission(mutableListOf(ACTIVITY_RECOGNITION).toTypedArray(), PERMISSION_ACTIVITY_RECOGNITION)
                                }
                            }else{
                                val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    val uri: Uri = Uri.fromParts("package", packageName, null)
                                    data = uri
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(openSettingsIntent)
                                settingState = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setNextOfFineLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(
                    this,
                    ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    setNextOfBackgroundLocationPermission()
                }else{
                    ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_background_location))
                    permissionNo = PERMISSION_ACCESS_BACKGROUND_LOCATION
                }
            }else{
                if(ContextCompat.checkSelfPermission(
                    this,
                    ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED){
                    setNextOfActivityRecognition()
                }else{
                    ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_useractivity))
                    permissionNo = PERMISSION_ACTIVITY_RECOGNITION
                }
            }

        } else{
            goToOnboardingActivity()
        }
    }

    private fun setNextOfBackgroundLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(
                    this,
                    ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED){
                setNextOfActivityRecognition()
            }else{
                // OS 29 이상 -> PERMISSION_ACTIVITY_RECOGNITION 요청 UI로 수정
                ivPermission.setImageDrawable(resources.getDrawable(R.drawable.permission_useractivity))
                permissionNo = PERMISSION_ACTIVITY_RECOGNITION
            }
        } else{
            goToOnboardingActivity()
        }
    }

    private fun setNextOfActivityRecognition(){
        goToOnboardingActivity()
    }

    private fun goToOnboardingActivity(){
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        if(settingState){
            setNextOfActivityRecognition()
        }
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