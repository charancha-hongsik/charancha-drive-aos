package com.charancha.drive.activity

import android.os.Bundle
import android.widget.ImageView
import com.charancha.drive.R

class DrivenDistanceActivity:BaseActivity() {
    lateinit var btn_back:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_distance)

        init()
        setResources()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)
    }

    private fun setResources(){
        btn_back.setOnClickListener { finish() }
    }
}