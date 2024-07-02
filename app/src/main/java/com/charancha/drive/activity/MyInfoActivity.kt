package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.CustomDialog
import com.charancha.drive.CustomDialogForEditText
import com.charancha.drive.R
import retrofit2.Callback
import retrofit2.Response

class MyInfoActivity:BaseActivity() {
    lateinit var btn_back:ImageView
    lateinit var tv_nickname:TextView
    lateinit var tv_withdrawal:TextView
    lateinit var ib_edit_nickname:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myinfo)

        init()
        setListener()
    }

    fun init(){
        btn_back = findViewById(R.id.btn_back)
        tv_nickname = findViewById(R.id.tv_nickname)
        tv_withdrawal = findViewById(R.id.tv_withdrawal)
        ib_edit_nickname = findViewById(R.id.ib_edit_nickname)

    }

    fun setListener(){
        btn_back.setOnClickListener {
            finish()
        }

        tv_withdrawal.setOnClickListener {

        }

        tv_nickname.setOnClickListener {

        }

        ib_edit_nickname.setOnClickListener {
            CustomDialogForEditText(this, "내 정보", "별명", "폭주하는 소금빵","저장","취소",  object : CustomDialogForEditText.DialogCallback{
                override fun onConfirm(contents:String) {

                }

                override fun onCancel() {


                }

            }).show()
        }


    }

    fun setResources(){

    }
}