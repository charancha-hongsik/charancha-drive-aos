package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WithdrawalActivity:BaseActivity() {
    lateinit var ib_terms1:ImageView
    lateinit var tv_confirm_withdrawal:TextView
    lateinit var tv_cancel_withdrawal:TextView
    lateinit var btn_back:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal)

        init()
        setListener()
    }

    fun init(){
        ib_terms1 = findViewById(R.id.ib_terms1)
        tv_confirm_withdrawal = findViewById(R.id.tv_confirm_withdrawal)
        tv_cancel_withdrawal = findViewById(R.id.tv_cancel_withdrawal)
        btn_back = findViewById(R.id.btn_back)

    }

    fun setListener(){
        ib_terms1.setOnClickListener {
            ib_terms1.isSelected = !ib_terms1.isSelected


        }

        tv_confirm_withdrawal.setOnClickListener {
            if(ib_terms1.isSelected){
                apiService().deleteAccount("Bearer " + PreferenceUtil.getPref(this@WithdrawalActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object:
                    Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code() == 200){
                            PreferenceUtil.putPref(this@WithdrawalActivity, PreferenceUtil.ACCESS_TOKEN, "")
                            PreferenceUtil.putPref(this@WithdrawalActivity, PreferenceUtil.REFRESH_TOKEN, "")
                            PreferenceUtil.putPref(this@WithdrawalActivity, PreferenceUtil.EXPIRES_IN, "")
                            PreferenceUtil.putPref(this@WithdrawalActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
                            PreferenceUtil.putPref(this@WithdrawalActivity, PreferenceUtil.TOKEN_TYPE, "")
                            startActivity(Intent(this@WithdrawalActivity, LoginActivity::class.java))
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    }

                })
            } else{
                Toast.makeText(this, "약관 동의 후 탈퇴가 가능합니다.",Toast.LENGTH_SHORT).show()
            }

        }

        tv_cancel_withdrawal.setOnClickListener {
            finish()
        }

        btn_back.setOnClickListener {
            finish()
        }
    }
}