package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.charancha.drive.R
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DetailManageScoreActivity:BaseActivity(){
    lateinit var tv_detail_managescroe_title: TextView
    lateinit var btn_back: ImageView
    lateinit var btn_choose_date: ImageView
    lateinit var layout_choose_date: CoordinatorLayout
    lateinit var persistent_bottom_sheet: LinearLayout
    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    lateinit var btn_close_select_date:ImageView
    lateinit var btn_a_month:TextView

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
        layout_choose_date = findViewById(R.id.layout_choose_date)
        persistent_bottom_sheet = findViewById(R.id.persistent_bottom_sheet)
        btn_close_select_date = findViewById(R.id.btn_close_select_date)
        btn_a_month = findViewById(R.id.btn_a_month)

        persistentBottomSheetEvent()

    }

    fun setResources(){
        tv_detail_managescroe_title.text = intent.getStringExtra("title")
        btn_a_month.isSelected = true
    }

    fun setListener(){
        btn_back.setOnClickListener { finish() }

        btn_choose_date.setOnClickListener {
            layout_choose_date.visibility = VISIBLE
        }

        layout_choose_date.setOnClickListener { layout_choose_date.visibility = GONE }

        btn_close_select_date.setOnClickListener {
            layout_choose_date.visibility = GONE
        }

    }

    private fun persistentBottomSheetEvent() {
        behavior = BottomSheetBehavior.from(persistent_bottom_sheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 되는 도중 계속 호출
                // called continuously while dragging
                Log.d("testset", "onStateChanged: 드래그 중")
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED-> {
                        Log.d("testset", "onStateChanged: 접음")
                    }
                    BottomSheetBehavior.STATE_DRAGGING-> {
                        Log.d("testset", "onStateChanged: 드래그")
                    }
                    BottomSheetBehavior.STATE_EXPANDED-> {
                        Log.d("testset", "onStateChanged: 펼침")
                    }
                    BottomSheetBehavior.STATE_HIDDEN-> {
                        Log.d("testset", "onStateChanged: 숨기기")
                    }
                    BottomSheetBehavior.STATE_SETTLING-> {
                        Log.d("testset", "onStateChanged: 고정됨")
                    }
                }
            }
        })
    }
}