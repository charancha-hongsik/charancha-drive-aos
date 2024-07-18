package com.charancha.drive.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R

class InputNameActivity: BaseRefreshActivity() {
    lateinit var et_name:EditText
    lateinit var btn_start: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_name)

        et_name = findViewById(R.id.et_name)
        btn_start = findViewById(R.id.btn_start)

        btn_start.setOnClickListener {
            if(et_name.text.toString() == ""){
                Toast.makeText(this, "이름을 입력해주세요",Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(this, "이름이 저장됐습니다.",Toast.LENGTH_SHORT).show()
                PreferenceUtil.putPref(this, PreferenceUtil.USER_NAME, et_name.text.toString())
                finish()
            }
        }

    }
}