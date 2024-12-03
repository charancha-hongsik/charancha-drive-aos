package com.milelog.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.milelog.PreferenceUtil
import com.milelog.R

class InputNameActivity: BaseRefreshActivity() {
    lateinit var et_name:EditText
    lateinit var btn_start: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_name)

        et_name = findViewById(R.id.et_name)
        btn_start = findViewById(R.id.btn_start)

        btn_start.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                if(et_name.text.toString() == ""){
                    showCustomToast(this@InputNameActivity, "이름을 입력해주세요")

                } else{
                    showCustomToast(this@InputNameActivity, "이름이 저장됐습니다.")

                    PreferenceUtil.putPref(this@InputNameActivity, PreferenceUtil.USER_NAME, et_name.text.toString())
                    finish()
                }
            }

        })

    }
}