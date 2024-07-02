package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R

class MyPageActivity:BaseActivity() {
    lateinit var layout_nickname:ConstraintLayout
    lateinit var btn_drive_history:ConstraintLayout
    lateinit var btn_alarm_setting:ConstraintLayout
    lateinit var btn_setting:ConstraintLayout
    lateinit var btn_terms:ConstraintLayout
    lateinit var btn_personal_info:ConstraintLayout
    lateinit var btn_logout: TextView
    lateinit var btn_back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        init()
        setListener()
    }

    fun init(){
        layout_nickname = findViewById(R.id.layout_nickname)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_alarm_setting = findViewById(R.id.btn_alarm_setting)
        btn_setting = findViewById(R.id.btn_setting)
        btn_terms = findViewById(R.id.btn_terms)
        btn_personal_info = findViewById(R.id.btn_personal_info)
        btn_logout = findViewById(R.id.btn_logout)
        btn_back = findViewById(R.id.btn_back)
    }

    fun setListener(){
        layout_nickname.setOnClickListener {
            startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java))
        }

        btn_drive_history.setOnClickListener {

        }

        btn_alarm_setting.setOnClickListener {

        }

        btn_setting.setOnClickListener {

        }

        btn_terms.setOnClickListener {

        }

        btn_personal_info.setOnClickListener {

        }

        btn_back.setOnClickListener { finish() }

        btn_logout.setOnClickListener {
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.ACCESS_TOKEN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.REFRESH_TOKEN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.EXPIRES_IN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.TOKEN_TYPE, "")
            startActivity(Intent(this@MyPageActivity, LoginActivity::class.java))
            finish()
        }


    }

    fun setResources(){

    }
}