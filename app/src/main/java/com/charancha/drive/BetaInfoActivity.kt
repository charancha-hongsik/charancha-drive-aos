package com.charancha.drive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BetaInfoActivity: AppCompatActivity() {
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_betainfo)

        init()
    }

    private fun init(){
        setResource()
        setListener()
    }

    private fun setResource(){
        btnStart = findViewById(R.id.btn_start)
    }

    private fun setListener(){
        btnStart.setOnClickListener {
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
        }
    }
}