package com.milelog.activity

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milelog.DividerItemDecoration
import com.milelog.R

class WinRewardHistoryActivity:BaseRefreshActivity() {
    lateinit var btn_back: View
    lateinit var layout_no_data:ConstraintLayout
    lateinit var lv_win_reward:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_reward)

        init()
        setListener()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)
        layout_no_data = findViewById(R.id.layout_no_data)
        lv_win_reward = findViewById(R.id.lv_win_reward)
    }

    private fun setListener(){
        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }
        })
    }

    private fun setBlank(){
        layout_no_data.visibility = VISIBLE
        lv_win_reward.visibility = GONE
    }

    private fun setRecyclerviewData(){
        lv_win_reward.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(
            this,
            R.color.gray_50,
            dpToPx(this, 32)
        ) // 색상 리소스와 구분선 높이 설정
        lv_win_reward.addItemDecoration(dividerItemDecoration)

        layout_no_data.visibility = GONE
        lv_win_reward.visibility = VISIBLE
    }
}