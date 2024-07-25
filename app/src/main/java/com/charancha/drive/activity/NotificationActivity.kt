package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.AgreeTermsRequest
import com.charancha.drive.retrofit.request.Agreements
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.retrofit.response.TermsSummaryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class NotificationActivity:BaseRefreshActivity() {
    lateinit var btn_all_noti: ImageView
    lateinit var btn_drive_history: ImageView
    lateinit var btn_marketing: ImageView
    lateinit var btn_announcement:ImageView
    lateinit var btn_back:ImageView
    lateinit var tv_marketing: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_notification)

        init()
    }

    fun init(){
        btn_all_noti = findViewById(R.id.btn_all_noti)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_marketing = findViewById(R.id.btn_marketing)
        btn_announcement = findViewById(R.id.btn_announcement)
        btn_back = findViewById(R.id.btn_back)
        tv_marketing = findViewById(R.id.tv_marketing)

        btn_all_noti.setOnClickListener {
            if(btn_all_noti.isSelected){
                btn_all_noti.isSelected = false
                btn_drive_history.isSelected = false
                if(btn_marketing.isSelected)
                    btn_marketing.performClick()
                btn_announcement.isSelected = false
            }else{
                btn_all_noti.isSelected = true
                btn_drive_history.isSelected = true

                if(!btn_marketing.isSelected)
                    btn_marketing.performClick()
                btn_announcement.isSelected = true
            }

        }

        btn_drive_history.setOnClickListener {
            if(btn_drive_history.isSelected){
                btn_all_noti.isSelected = false
                btn_drive_history.isSelected = false
            }else{
                if(btn_marketing.isSelected && btn_announcement.isSelected){
                    btn_all_noti.isSelected = true
                }

                btn_drive_history.isSelected = true

            }

        }

        btn_marketing.setOnClickListener(object:OnSingleClickListener() {
            override fun onSingleClick(v: View?) {

                if(btn_marketing.isSelected){
                    apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if(response.code() == 200 || response.code() == 201){
                                val jsonString = response.body()?.string()

                                val gson = Gson()
                                val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                                val termsSummaryResponse: List<TermsSummaryResponse> = gson.fromJson(jsonString, type)

                                for(term in termsSummaryResponse){
                                    if(term.title.contains(tv_marketing.text)){
                                        putTerms(term.id, 0)
                                    }
                                }
                            }else{

                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                        }

                    })
                }else{
                    apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if(response.code() == 200 || response.code() == 201){
                                val jsonString = response.body()?.string()

                                val gson = Gson()
                                val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                                val termsSummaryResponse: List<TermsSummaryResponse> = gson.fromJson(jsonString, type)

                                for(term in termsSummaryResponse){
                                    if(term.title.contains(tv_marketing.text)){
                                        putTerms(term.id, 1)
                                    }
                                }
                            }else{

                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                        }

                    })
                }
            }

        })

        btn_announcement.setOnClickListener {
            if(btn_announcement.isSelected){
                btn_all_noti.isSelected = false
                btn_announcement.isSelected = false
            }else{
                if(btn_drive_history.isSelected && btn_marketing.isSelected){
                    btn_all_noti.isSelected = true
                }

                btn_announcement.isSelected = true
            }


        }

        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun putTerms(id:String, isAgree:Int){
        val acceptedTerms = mutableListOf<Agreements>()
        acceptedTerms.add(Agreements(id,isAgree))


        val gson = Gson()
        val jsonParam = gson.toJson(AgreeTermsRequest(acceptedTerms.toList()))

        apiService().postTermsAgree("Bearer " + PreferenceUtil.getPref(this@NotificationActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    if(PreferenceUtil.getBooleanPref(this@NotificationActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                        apiService().getMyCarInfo("Bearer " + PreferenceUtil.getPref(this@NotificationActivity, PreferenceUtil.ACCESS_TOKEN, "")).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200){
                                    if(isAgree == 0){
                                        btn_all_noti.isSelected = false
                                        btn_marketing.isSelected = false
                                        Toast.makeText(this@NotificationActivity, getTodayFormattedDate() + " 마일로그 마케팅 정보 수신 거부되었습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                    else{
                                        if(btn_drive_history.isSelected && btn_announcement.isSelected){
                                            btn_all_noti.isSelected = true
                                        }

                                        btn_marketing.isSelected = true

                                        Toast.makeText(this@NotificationActivity, getTodayFormattedDate() + " 마일로그 마케팅 정보 수신 동의되었습니다.", Toast.LENGTH_SHORT).show()

                                    }



                                }else{

                                }
                            }

                            override fun onFailure(
                                call: Call<ResponseBody>,
                                t: Throwable
                            ) {

                            }
                        })
                    }else{

                    }
                } else{
                    Toast.makeText(this@NotificationActivity,"통신 실패",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }
}