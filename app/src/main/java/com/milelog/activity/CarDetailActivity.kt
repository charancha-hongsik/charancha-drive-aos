package com.milelog.activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.milelog.R
import com.milelog.retrofit.response.PostMyCarResponse

class CarDetailActivity: BaseRefreshActivity() {
    private lateinit var postMyCarResponse: PostMyCarResponse
    lateinit var tv_confirm_mycar_info1:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_detail)

        init()
        setInfo()
    }

    private fun init(){
        tv_confirm_mycar_info1 = findViewById(R.id.tv_confirm_mycar_info1)
    }

    private fun setInfo(){
        val jsonString = intent.getStringExtra("carInfo")
        val gson = Gson()
        postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

        tv_confirm_mycar_info1.text = postMyCarResponse.carName + "\n차량명이 맞으신가요?"
    }
}