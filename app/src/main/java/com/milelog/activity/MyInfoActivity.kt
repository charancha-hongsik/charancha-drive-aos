package com.milelog.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.milelog.CustomDialogForEditText
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.PatchProfilesRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyInfoActivity: BaseRefreshActivity() {
    lateinit var btn_back:ImageView
    lateinit var tv_nickname:TextView
    lateinit var tv_withdrawal:TextView
    lateinit var ib_edit_nickname:ImageView
    lateinit var nickName:String
    lateinit var tv_login_oauth:TextView
    lateinit var tv_email:TextView

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
        tv_login_oauth = findViewById(R.id.tv_login_oauth)
        tv_email = findViewById(R.id.tv_email)

        nickName = intent.getStringExtra("nickname")!!
        tv_nickname.text = nickName
        tv_login_oauth.text = intent.getStringExtra("provider")!!
        tv_email.text = intent.getStringExtra("email")

    }

    fun setListener(){
        btn_back.setOnClickListener {
            finish()
        }

        tv_withdrawal.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyInfoActivity, WithdrawalActivity::class.java))
            }

        })

        ib_edit_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                CustomDialogForEditText(this@MyInfoActivity, "내 정보", "별명", nickName,"저장","취소",  object : CustomDialogForEditText.DialogCallback{
                    override fun onConfirm(contents:String) {
                        val gson = Gson()
                        val jsonParam =
                            gson.toJson(PatchProfilesRequest(contents))

                        apiService().patchAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyInfoActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200 || response.code() == 201){
                                    showCustomToast(this@MyInfoActivity, "저장 되었습니다.")

                                    tv_nickname.text = contents

                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                            }

                        })
                    }

                    override fun onCancel() {

                    }

                }).show()            }

        })
    }

    fun setResources(){

    }
}