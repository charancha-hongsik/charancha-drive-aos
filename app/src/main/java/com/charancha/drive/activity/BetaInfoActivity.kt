package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R

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