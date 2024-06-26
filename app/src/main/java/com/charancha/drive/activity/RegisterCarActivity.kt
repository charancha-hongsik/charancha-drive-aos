package com.charancha.drive.activity

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R


class RegisterCarActivity: AppCompatActivity() {
    lateinit var view_register_percent1: View
    lateinit var view_register_percent2:View
    lateinit var view_register_percent3:View
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var tv_register_car_hint: TextView
    lateinit var et_register_car: EditText




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_car)

        init()
    }

    fun init(){
        setResources()
    }

    fun setResources(){
        view_register_percent1 = findViewById(R.id.view_register_percent1)
        view_register_percent2 = findViewById(R.id.view_register_percent2)
        view_register_percent3 = findViewById(R.id.view_register_percent3)

        ib_arrow_register_car = findViewById(R.id.ib_arrow_register_car)
        tv_register_car_hint = findViewById(R.id.tv_register_car_hint)
        et_register_car = findViewById(R.id.et_register_car)

        view_register_percent1.isSelected = true
        et_register_car.setOnFocusChangeListener { view, b ->
            if(b){
                tv_register_car_hint.visibility = GONE
            }else{
                tv_register_car_hint.visibility = VISIBLE

            }
        }
    }
}