package com.charancha.drive.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.R

class DetailManageScoreActivity:BaseActivity(){
    lateinit var tv_detail_managescroe_title: TextView
    lateinit var btn_back: ImageView
    lateinit var btn_choose_date: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_managescore)

        init()
        setResources()
        setListener()
    }

    fun init(){
        tv_detail_managescroe_title = findViewById(R.id.tv_detail_managescroe_title)
        btn_back = findViewById(R.id.btn_back)
        btn_choose_date = findViewById(R.id.btn_choose_date)

    }

    fun setResources(){
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
    }

    fun setListener(){
        btn_back.setOnClickListener { finish() }

        btn_choose_date.setOnClickListener {

        }
    }
}