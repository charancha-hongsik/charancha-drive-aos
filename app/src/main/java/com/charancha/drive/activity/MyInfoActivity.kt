package com.charancha.drive.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R

class MyInfoActivity:BaseActivity() {
    lateinit var btn_back:ImageView
    lateinit var et_login_oauth:EditText
    lateinit var et_email:EditText
    lateinit var et_name:EditText
    lateinit var et_nickname:EditText
    lateinit var tv_withdrawal:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myinfo)

        init()
        setListener()
    }

    fun init(){
        btn_back = findViewById(R.id.btn_back)
        et_login_oauth = findViewById(R.id.et_login_oauth)
        et_email = findViewById(R.id.et_email)
        et_name = findViewById(R.id.et_name)
        et_nickname = findViewById(R.id.et_nickname)
        tv_withdrawal = findViewById(R.id.tv_withdrawal)

        btn_back.setOnClickListener {
            finish()
        }

    }

    fun setListener(){



    }

    fun setResources(){

    }
}