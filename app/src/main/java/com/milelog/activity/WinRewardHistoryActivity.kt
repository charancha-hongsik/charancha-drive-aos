package com.milelog.activity

import android.os.Bundle
import android.view.View
import com.milelog.R

class WinRewardHistoryActivity:BaseRefreshActivity() {
    lateinit var btn_back: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_reward)

        init()
        setListener()

    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)
    }

    private fun setListener(){
        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }

        })
    }
}