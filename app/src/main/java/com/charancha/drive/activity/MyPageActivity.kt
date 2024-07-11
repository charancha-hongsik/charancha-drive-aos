package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetAccountProfilesResponse
import com.charancha.drive.retrofit.response.GetAccountResponse
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageActivity:BaseActivity() {
    lateinit var layout_nickname:ConstraintLayout
    lateinit var btn_drive_history:ConstraintLayout
    lateinit var btn_alarm_setting:ConstraintLayout
    lateinit var btn_setting:ConstraintLayout
    lateinit var btn_terms:ConstraintLayout
    lateinit var btn_personal_info:ConstraintLayout
    lateinit var btn_logout: TextView
    lateinit var btn_back: ImageView
    lateinit var getAccountProfilesResponse:GetAccountProfilesResponse
    lateinit var tv_email:TextView
    lateinit var tv_nickname:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        init()
        setListener()
    }

    fun init(){
        layout_nickname = findViewById(R.id.layout_nickname)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_alarm_setting = findViewById(R.id.btn_alarm_setting)
        btn_setting = findViewById(R.id.btn_setting)
        btn_terms = findViewById(R.id.btn_terms)
        btn_personal_info = findViewById(R.id.btn_personal_info)
        btn_logout = findViewById(R.id.btn_logout)
        btn_back = findViewById(R.id.btn_back)
        tv_email = findViewById(R.id.tv_email)
        tv_nickname = findViewById(R.id.tv_nickname)
    }

    fun setListener(){
        layout_nickname.setOnClickListener {
            startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en))
        }

        btn_drive_history.setOnClickListener {

        }

        btn_alarm_setting.setOnClickListener {

        }

        btn_setting.setOnClickListener {

        }

        btn_terms.setOnClickListener {

        }

        btn_personal_info.setOnClickListener {

        }

        btn_back.setOnClickListener { finish() }

        btn_logout.setOnClickListener {
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.ACCESS_TOKEN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.REFRESH_TOKEN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.EXPIRES_IN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.TOKEN_TYPE, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.KEYLESS_ACCOUNT, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.KEYLESS_ACCOUNT_EXPIRE, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.OAUTH_PROVIDER, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.ID_TOKEN, "")
            PreferenceUtil.putPref(this@MyPageActivity, PreferenceUtil.ACCOUNT_ADDRESS, "")
            startActivity(Intent(this@MyPageActivity, LoginActivity::class.java))
            finish()
        }


    }

    override fun onResume() {
        super.onResume()
        setResources()
    }

    fun setResources(){
        apiService().getAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyPageActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                Log.d("testestseest","testestest :: " + response.code())

                if(response.code() == 200){
                    getAccountProfilesResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetAccountProfilesResponse::class.java
                    )

                    tv_email.text = getAccountProfilesResponse.user.email
                    tv_nickname.text = getAccountProfilesResponse.nickName + "님"

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}