package com.milelog.activity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.gson.Gson
import com.milelog.R
import com.milelog.retrofit.response.PostMyCarResponse

class CarDetailActivity: BaseRefreshActivity() {
    private lateinit var postMyCarResponse: PostMyCarResponse
    lateinit var tv_confirm_mycar_info1:TextView
    lateinit var listView: ListView
    lateinit var layout_select:CoordinatorLayout
    lateinit var btn_maker:ConstraintLayout
    lateinit var btn_model:ConstraintLayout
    lateinit var btn_model_detail:ConstraintLayout
    lateinit var btn_grade:ConstraintLayout
    lateinit var btn_grade_detail:ConstraintLayout
    lateinit var tv_maker_hint:TextView
    lateinit var tv_maker:TextView
    lateinit var tv_model_hint:TextView
    lateinit var tv_model:TextView
    lateinit var tv_model_detail_hint:TextView
    lateinit var tv_model_detail:TextView
    lateinit var tv_grade_hint:TextView
    lateinit var tv_grade:TextView
    lateinit var tv_grade_detail_hint:TextView
    lateinit var tv_grade_detail:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_detail)

        init()
        setListener()
        setInfo()
    }

    private fun init(){
        tv_confirm_mycar_info1 = findViewById(R.id.tv_confirm_mycar_info1)
        layout_select = findViewById(R.id.layout_select)
        listView = findViewById(R.id.listView)
        btn_maker = findViewById(R.id.btn_maker)
        btn_model = findViewById(R.id.btn_model)
        btn_model_detail = findViewById(R.id.btn_model_detail)
        btn_grade = findViewById(R.id.btn_grade)
        btn_grade_detail = findViewById(R.id.btn_grade_detail)
        tv_maker_hint = findViewById(R.id.tv_maker_hint)
        tv_maker = findViewById(R.id.tv_maker)
        tv_model_hint = findViewById(R.id.tv_model_hint)
        tv_model = findViewById(R.id.tv_model)
        tv_model_detail_hint = findViewById(R.id.tv_model_detail_hint)
        tv_model_detail = findViewById(R.id.tv_model_detail)
        tv_grade_hint = findViewById(R.id.tv_grade_hint)
        tv_grade = findViewById(R.id.tv_grade)
        tv_grade_detail_hint = findViewById(R.id.tv_grade_detail_hint)
        tv_grade_detail = findViewById(R.id.tv_grade_detail)
    }

    private fun setListener(){
        btn_maker.setOnClickListener {
            setSelector()
            layout_select.visibility = VISIBLE
        }

        btn_model.setOnClickListener {
            setSelector()
            layout_select.visibility = VISIBLE

        }

        btn_model_detail.setOnClickListener {
            setSelector()
            layout_select.visibility = VISIBLE

        }

        btn_grade.setOnClickListener {
            setSelector()
            layout_select.visibility = VISIBLE

        }

        btn_grade_detail.setOnClickListener {
            setSelector()
            layout_select.visibility = VISIBLE

        }

        layout_select.setOnClickListener{
            layout_select.visibility = GONE
        }

    }

    private fun setInfo(){
        val jsonString = intent.getStringExtra("carInfo")
        val gson = Gson()
        postMyCarResponse = gson.fromJson(jsonString, PostMyCarResponse::class.java)

        tv_confirm_mycar_info1.text = postMyCarResponse.carName + "\n차량명이 맞으신가요?"
    }

    private fun setSelector(){
        val itemList: MutableList<String?> = ArrayList()

        // 데이터 추가
        itemList.add("가솔린")
        itemList.add("디젤")
        itemList.add("LPG")
        itemList.add("전기")
        itemList.add("수소")
        itemList.add("CNG")
        itemList.add("가솔린+LPG")
        itemList.add("가솔린+CNG")
        itemList.add("가솔린+전기")
        itemList.add("디젤+전기")
        itemList.add("LPG+전기")
        itemList.add("기타")


        // adapter 생성
        val adapter = ArrayAdapter(this, R.layout.edit_fuel_textview, R.id.tv_fuel, itemList)


        // listView에 adapter 연결
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, l ->
            val fuel = parent.getItemAtPosition(position) as String
            layout_select.visibility = GONE
        }
    }
}