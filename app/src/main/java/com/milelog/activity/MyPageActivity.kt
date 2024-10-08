package com.milelog.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetAccountProfilesResponse
import com.milelog.retrofit.response.TermsSummaryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class MyPageActivity: BaseRefreshActivity() {
    lateinit var layout_nickname:ConstraintLayout
    lateinit var btn_drive_history:ConstraintLayout
    lateinit var btn_alarm_setting:ConstraintLayout
    lateinit var btn_setting:ConstraintLayout
    lateinit var btn_terms:ConstraintLayout
    lateinit var btn_personal_info:ConstraintLayout
    lateinit var btn_logout: TextView
    lateinit var btn_back: ImageView
    lateinit var getAccountProfilesResponse: GetAccountProfilesResponse
    lateinit var tv_email:TextView
    lateinit var tv_nickname:TextView
    lateinit var termsSummaryResponse: List<TermsSummaryResponse>


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

        apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val gson = Gson()
                    val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                    termsSummaryResponse = gson.fromJson(jsonString, type)

                }else if(response.code() == 401){
                    logout()
                } else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setListener(){
        layout_nickname.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyInfoActivity::class.java).putExtra("nickname",getAccountProfilesResponse.nickName).putExtra("email", getAccountProfilesResponse.user.email).putExtra("provider",getAccountProfilesResponse.user.provider.text.en))
            }

        })

        btn_drive_history.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, MyDriveHistoryActivity::class.java))
            }

        })

        btn_alarm_setting.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, NotificationActivity::class.java))
            }

        })

        btn_setting.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyPageActivity, SettingActivity::class.java))
            }
        })


        btn_terms.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(term.title.contains("이용약관")){
                        startActivity(Intent(this@MyPageActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }

        })

        btn_personal_info.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(term.title.contains("개인정보 수집 및 이용 동의")){
                        if(term.isRequired == 1)
                            startActivity(Intent(this@MyPageActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }

        })


        btn_back.setOnClickListener { finish() }

        btn_logout.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                logout()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        setResources()
    }

    fun setResources(){
        apiService().getAccountProfiles("Bearer " + PreferenceUtil.getPref(this@MyPageActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if(response.code() == 200 || response.code() == 201){
                    getAccountProfilesResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetAccountProfilesResponse::class.java
                    )

                    tv_email.text = getAccountProfilesResponse.user.email
                    tv_nickname.text = getAccountProfilesResponse.nickName + "님"

                }else if(response.code() == 401){
                    logout()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }
}