package com.charancha.drive.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.R

class DetailManageScoreActivity:BaseActivity(){
    lateinit var tv_detail_managescroe_title: TextView
    lateinit var btn_back: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_managescore)

        init()
    }

    fun init(){
        tv_detail_managescroe_title = findViewById(R.id.tv_detail_managescroe_title)
        btn_back = findViewById(R.id.btn_back)
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
        btn_back.setOnClickListener { finish() }


    }
}