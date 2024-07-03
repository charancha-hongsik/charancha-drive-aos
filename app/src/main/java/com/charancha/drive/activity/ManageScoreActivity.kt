package com.charancha.drive.activity

import android.os.Bundle
import android.widget.ImageView
import com.charancha.drive.R

class ManageScoreActivity:BaseActivity() {
    lateinit var btn_back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_managescore)

        init()

    }

    fun init(){
        setResources()
    }

    fun setResources(){
        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener {
            finish()
        }
    }
}