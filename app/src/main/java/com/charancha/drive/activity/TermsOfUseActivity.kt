package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.CommonUtil
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.request.AgreeTermsRequest
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


class TermsOfUseActivity: BaseActivity() {
    private lateinit var ibArrowTerms:ImageButton
    private lateinit var btnNext:ConstraintLayout
    private lateinit var ibAllAccept:ImageButton
    private lateinit var ibTerms1:ImageButton
    private lateinit var ibTerms2:ImageButton
    private lateinit var ibTerms3:ImageButton
    private lateinit var ibTerms4:ImageButton
    private lateinit var tvTerms1:TextView
    private lateinit var tvTermsTitle1:TextView
    private lateinit var tvTermsTitle2:TextView
    private lateinit var tvTermsTitle3:TextView
    private lateinit var tvTermsTitle4:TextView

    lateinit var termsSummaryResponse: List<TermsSummaryResponse>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        init()
    }

    private fun init(){
        apiService().getTerms("마일로그_서비스").enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val jsonString = response.body()?.string()

                    val gson = Gson()
                    val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                    termsSummaryResponse = gson.fromJson(jsonString, type)

                    setResource()
                    setListener()
                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onBackPressed() {
        PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.ACCESS_TOKEN, "")
        PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.REFRESH_TOKEN, "")
        PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.EXPIRES_IN, "")
        PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
        PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.TOKEN_TYPE, "")
        super.onBackPressed()
    }

    private fun setResource(){
        ibArrowTerms = findViewById(R.id.ib_arrow_terms)
        btnNext = findViewById(R.id.btn_next)
        ibAllAccept = findViewById(R.id.ib_all_accept)
        ibTerms1 = findViewById(R.id.ib_terms1)
        ibTerms2 = findViewById(R.id.ib_terms2)
        ibTerms3 = findViewById(R.id.ib_terms3)
        ibTerms4 = findViewById(R.id.ib_terms4)
        tvTerms1 = findViewById(R.id.tv_terms1)
        tvTermsTitle1 = findViewById(R.id.tv_terms_title1)
        tvTermsTitle2 = findViewById(R.id.tv_terms_title2)
        tvTermsTitle3 = findViewById(R.id.tv_terms_title3)
        tvTermsTitle4 = findViewById(R.id.tv_terms_title4)


        // TextView에 SpannableString 설정
        tvTerms1.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.first_terms_text), resources.getString(R.string.first_terms_text_red), resources.getColor(R.color.pri_500))
        tvTermsTitle1.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title1), resources.getString(R.string.terms_title1_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle2.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title2), resources.getString(R.string.terms_title2_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle3.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title3), resources.getString(R.string.terms_title3_gray), resources.getColor(R.color.gray_400))
        tvTermsTitle4.text = CommonUtil.getSpannableString(this@TermsOfUseActivity, resources.getString(R.string.terms_title4), resources.getString(R.string.terms_title4_gray), resources.getColor(R.color.gray_400))

    }

    private fun setListener(){
        ibArrowTerms.setOnClickListener {
            PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.ACCESS_TOKEN, "")
            PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.REFRESH_TOKEN, "")
            PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.EXPIRES_IN, "")
            PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
            PreferenceUtil.putPref(this@TermsOfUseActivity, PreferenceUtil.TOKEN_TYPE, "")
            finish()
        }

        btnNext.setOnClickListener {
            val acceptedTerms = mutableListOf<String>()

            for(term in termsSummaryResponse){

                if(tvTermsTitle4.text.contains(term.title)){
                    if(ibTerms4.isSelected){
                        acceptedTerms.add(term.id)
                    }
                }else{
                    acceptedTerms.add(term.id)
                }
            }

            val gson = Gson()
            val jsonParam = gson.toJson(AgreeTermsRequest(acceptedTerms.toList()))

            apiService().postTermsAgree("Bearer " + PreferenceUtil.getPref(this@TermsOfUseActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d("testestsetset","testsetestse response.code :: " + response.code())
                    if(response.code() == 200 || response.code() == 201){
                        if(PreferenceUtil.getBooleanPref(this@TermsOfUseActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                            startActivity(Intent(this@TermsOfUseActivity, OnBoardingActivity::class.java))
                            finish()
                        }else{
                            startActivity(Intent(this@TermsOfUseActivity, PermissionInfoActivity::class.java))
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

        btnNext.isClickable = false

        ibAllAccept.setOnClickListener {
            ibAllAccept.isSelected = !ibAllAccept.isSelected

            ibTerms1.isSelected = ibAllAccept.isSelected
            ibTerms2.isSelected = ibAllAccept.isSelected
            ibTerms3.isSelected = ibAllAccept.isSelected
            ibTerms4.isSelected = ibAllAccept.isSelected

            checkAllAccept()
        }

        ibTerms1.setOnClickListener {
            ibTerms1.isSelected = !ibTerms1.isSelected

            if(!ibTerms1.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }

        ibTerms2.setOnClickListener {
            ibTerms2.isSelected = !ibTerms2.isSelected

            if(!ibTerms2.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }
        ibTerms3.setOnClickListener {
            ibTerms3.isSelected = !ibTerms3.isSelected

            if(!ibTerms3.isSelected)
                ibAllAccept.isSelected = false

            checkAllAccept()
        }

        ibTerms4.setOnClickListener {
            ibTerms4.isSelected = !ibTerms4.isSelected
        }

        tvTermsTitle2.setOnClickListener{
            for(term in termsSummaryResponse){
                if(tvTermsTitle2.text.contains(term.title)){
                    startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                }
            }
        }

        tvTermsTitle3.setOnClickListener{
            for(term in termsSummaryResponse){
                if(tvTermsTitle3.text.contains(term.title)){
                    startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                }
            }
        }

        tvTermsTitle4.setOnClickListener{
            for(term in termsSummaryResponse){
                if(tvTermsTitle4.text.contains(term.title)){
                    startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                }
            }
        }
    }

    private fun checkAllAccept(){
        btnNext.isSelected = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected
        btnNext.isClickable = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected
    }
}