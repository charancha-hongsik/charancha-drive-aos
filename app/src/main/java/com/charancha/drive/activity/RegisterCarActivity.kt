package com.charancha.drive.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.charancha.drive.R


class RegisterCarActivity: AppCompatActivity() {
    lateinit var view_register_percent1: View
    lateinit var view_register_percent2:View
    lateinit var view_register_percent3:View
    lateinit var ib_arrow_register_car:ImageButton
    lateinit var tv_register_car_hint: TextView
    lateinit var et_register_car: EditText
    lateinit var btn_next:ConstraintLayout
    lateinit var tv_register_car:TextView
    lateinit var tv_register_car_caution:TextView

    var no = 0

    var carNo:String? = null
    var carOwner:String? = null



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

        tv_register_car = findViewById(R.id.tv_register_car)
        tv_register_car_caution = findViewById(R.id.tv_register_car_caution)

        btn_next = findViewById(R.id.btn_next)
        btn_next.setOnClickListener {
            no++

            when(no){
                1 -> {
                    setCarOwnerPage()
                }

                2 -> {

                }

                3 -> {

                }
            }
        }

        view_register_percent1.isSelected = true
        et_register_car.setOnFocusChangeListener { view, b ->
            if(b){
                tv_register_car_hint.visibility = GONE
            }else{
                tv_register_car_hint.visibility = VISIBLE

            }
        }

        et_register_car.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p1==0){
                    btn_next.isSelected = false
                    btn_next.isClickable = false
                }else{
                    btn_next.isSelected = true
                    btn_next.isClickable = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    fun setCarNoPage(){
        tv_register_car.text = resources.getString(R.string.register_car_no_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_no_errormessage)
        carNo = null
    }

    fun setCarOwnerPage(){
        tv_register_car.text = resources.getString(R.string.register_car_owner_title)
        tv_register_car_caution.text = resources.getString(R.string.register_car_owner_errormessage)
        carOwner = null
    }



    override fun onBackPressed() {
        when(no){
            0 -> {
                setCarNoPage()
            }

            1 -> {
                setCarOwnerPage()
            }

            2 -> {

            }

            3 -> {

            }
        }

        super.onBackPressed()
    }
}