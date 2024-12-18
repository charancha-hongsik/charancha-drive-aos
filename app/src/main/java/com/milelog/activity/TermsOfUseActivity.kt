package com.milelog.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.milelog.CommonUtil
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.AgreeTermsRequest
import com.milelog.retrofit.request.Agreements
import com.milelog.retrofit.response.TermsSummaryResponse
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type


/**
 * 뒤로가기 시 로그아웃 및 로그인 창으로 이동
 * 모두 허용 후 다음 클릭 시 퍼미션 창으로 이동
 */
class TermsOfUseActivity: BaseActivity() {
    private lateinit var ibArrowTerms:ImageButton
    private lateinit var btnNext:ConstraintLayout
    private lateinit var ibAllAccept:ImageButton
    private lateinit var ibTerms1:ImageButton
    private lateinit var ibTerms2:ImageButton
    private lateinit var ibTerms3:ImageButton
    private lateinit var ibTerms4:ImageButton
    private lateinit var ibTerms5:ImageButton
    private lateinit var ibTerms6:ImageButton

    private lateinit var tvTerms1:TextView
    private lateinit var tvTermsTitle1:TextView
    private lateinit var tvTermsTitle2:TextView
    private lateinit var tvTermsTitle3:TextView
    private lateinit var tvTermsTitle4:TextView
    private lateinit var tvTermsTitle5:TextView
    private lateinit var tvTermsTitle6:TextView


    lateinit var termsSummaryResponse: List<TermsSummaryResponse>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        init()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun init(){
        apiService().getTerms("MILELOG_USAGE").enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try{
                    if(response.code() == 200 || response.code() == 201){
                        val jsonString = response.body()?.string()

                        val gson = GsonBuilder().serializeNulls().create()
                        val type: Type = object : TypeToken<List<TermsSummaryResponse?>?>() {}.type
                        termsSummaryResponse = gson.fromJson(jsonString, type)

                        setResource()
                        setListener()
                    }else if(response.code() == 401){
                        logout()
                    } else{

                    }
                }catch(e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    override fun onBackPressed() {
        logout()
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
        ibTerms5 = findViewById(R.id.ib_terms5)
        ibTerms6 = findViewById(R.id.ib_terms6)
        tvTerms1 = findViewById(R.id.tv_terms1)
        tvTermsTitle1 = findViewById(R.id.tv_terms_title1)
        tvTermsTitle2 = findViewById(R.id.tv_terms_title2)
        tvTermsTitle3 = findViewById(R.id.tv_terms_title3)
        tvTermsTitle4 = findViewById(R.id.tv_terms_title4)
        tvTermsTitle5 = findViewById(R.id.tv_terms_title5)
        tvTermsTitle6 = findViewById(R.id.tv_terms_title6)


        try {
            // TextView에 SpannableString 설정
            tvTerms1.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.first_terms_text),
                resources.getString(R.string.first_terms_text_red),
                resources.getColor(R.color.pri_800)
            )
            tvTermsTitle1.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title1),
                resources.getString(R.string.terms_title1_gray),
                resources.getColor(R.color.gray_400)
            )
            tvTermsTitle2.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title2),
                resources.getString(R.string.terms_title2_gray),
                resources.getColor(R.color.gray_400)
            )
            tvTermsTitle3.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title3),
                resources.getString(R.string.terms_title3_gray),
                resources.getColor(R.color.gray_400)
            )
            tvTermsTitle4.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title4),
                resources.getString(R.string.terms_title4_gray),
                resources.getColor(R.color.gray_400)
            )
            tvTermsTitle5.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title5),
                resources.getString(R.string.terms_title5_gray),
                resources.getColor(R.color.gray_400)
            )

            tvTermsTitle6.text = CommonUtil.getSpannableString(
                this@TermsOfUseActivity,
                resources.getString(R.string.terms_title6),
                resources.getString(R.string.terms_title6_gray),
                resources.getColor(R.color.gray_400)
            )

            val content2 = SpannableString(tvTermsTitle2.text.toString())
            content2.setSpan(UnderlineSpan(), 0, content2.length - 5, 0)
            tvTermsTitle2.text = content2

            val content3 = SpannableString(tvTermsTitle3.text.toString())
            content3.setSpan(UnderlineSpan(), 0, content3.length - 5, 0)
            tvTermsTitle3.text = content3

            val content4 = SpannableString(tvTermsTitle4.text.toString())
            content4.setSpan(UnderlineSpan(), 0, content4.length - 5, 0)
            tvTermsTitle4.text = content4

            val content5 = SpannableString(tvTermsTitle5.text.toString())
            content5.setSpan(UnderlineSpan(), 0, content5.length - 5, 0)
            tvTermsTitle5.text = content5

            val content6 = SpannableString(tvTermsTitle6.text.toString())
            content6.setSpan(UnderlineSpan(), 0, content6.length - 5, 0)
            tvTermsTitle6.text = content6


        }catch (e:Exception){

        }


    }


    private fun setListener(){
        ibArrowTerms.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                logout()
            }

        })

        btnNext.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                val acceptedTerms = mutableListOf<Agreements>()


                for(term in termsSummaryResponse){
                    if(tvTermsTitle4.text.contains(term.title)){
                        if(!term.isRequired)
                            if(ibTerms4.isSelected){
                                acceptedTerms.add(Agreements(term.id,true))
                            }else {
                                acceptedTerms.add(Agreements(term.id, false))
                            }
                        else{
                            acceptedTerms.add(Agreements(term.id,true))
                        }
                    } else if(tvTermsTitle5.text.contains(term.title)){
                        if(ibTerms5.isSelected){
                            acceptedTerms.add(Agreements(term.id,true))
                        }else {
                            acceptedTerms.add(Agreements(term.id, false))
                        }
                    } else{
                        acceptedTerms.add(Agreements(term.id,true))
                    }
                }

                val gson = GsonBuilder().serializeNulls().create()
                val jsonParam = gson.toJson(AgreeTermsRequest(acceptedTerms.toList()))

                apiService().postTermsAgree("Bearer " + PreferenceUtil.getPref(this@TermsOfUseActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        try{
                            if(response.code() == 200 || response.code() == 201){
                                if(PreferenceUtil.getBooleanPref(this@TermsOfUseActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                                    apiService().getMyCarCount("Bearer " + PreferenceUtil.getPref(this@TermsOfUseActivity, PreferenceUtil.ACCESS_TOKEN, "")).enqueue(object :Callback<ResponseBody>{
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            try{
                                                if(response.code() == 200 || response.code() == 201){
                                                    val jsonString = response.body()?.string()

                                                    if(ibTerms5.isSelected){
                                                        showCustomToast(this@TermsOfUseActivity,getTodayFormattedDate() + " 마일로그 마케팅 정보 수신 동의되었습니다.")
                                                    }else{
                                                        showCustomToast(this@TermsOfUseActivity,getTodayFormattedDate() + " 마일로그 마케팅 정보 수신 거부되었습니다.")

                                                    }


                                                    if(jsonString!!.toInt() > 0){
                                                        startActivity(Intent(this@TermsOfUseActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

                                                        finish()
                                                    }else{
                                                        startActivity(Intent(this@TermsOfUseActivity, OnBoardingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                                        finish()
                                                    }
                                                }else if(response.code() == 401){
                                                    logout()
                                                } else{
                                                    startActivity(Intent(this@TermsOfUseActivity, OnBoardingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                                    finish()
                                                }
                                            }catch(e:Exception){

                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<ResponseBody>,
                                            t: Throwable
                                        ) {
                                            startActivity(Intent(this@TermsOfUseActivity, OnBoardingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                            finish()
                                        }
                                    })
                                }else{
                                    startActivity(Intent(this@TermsOfUseActivity, PermissionInfoActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                    finish()
                                }
                            } else if(response.code() == 401){
                                logout()
                            } else{
                                showCustomToast(this@TermsOfUseActivity,"통신 실패")

                            }
                        }catch (e:Exception){

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }

                })
            }

        })


        btnNext.isClickable = false

        ibAllAccept.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibAllAccept.isSelected = !ibAllAccept.isSelected

                ibTerms1.isSelected = ibAllAccept.isSelected
                ibTerms2.isSelected = ibAllAccept.isSelected
                ibTerms3.isSelected = ibAllAccept.isSelected
                ibTerms4.isSelected = ibAllAccept.isSelected
                ibTerms5.isSelected = ibAllAccept.isSelected
                ibTerms6.isSelected = ibAllAccept.isSelected

                checkAllAccept()
            }
        })


        ibTerms1.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms1.isSelected = !ibTerms1.isSelected

                if(!ibTerms1.isSelected)
                    ibAllAccept.isSelected = false

                checkAllAccept()
            }

        })

        ibTerms2.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms2.isSelected = !ibTerms2.isSelected

                if(!ibTerms2.isSelected)
                    ibAllAccept.isSelected = false

                checkAllAccept()
            }

        })

        ibTerms3.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms3.isSelected = !ibTerms3.isSelected

                if(!ibTerms3.isSelected)
                    ibAllAccept.isSelected = false

                checkAllAccept()
            }

        })

        ibTerms4.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms4.isSelected = !ibTerms4.isSelected

                checkAllAccept()

            }

        })

        ibTerms5.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms5.isSelected = !ibTerms5.isSelected

                checkAllAccept()
            }

        })

        ibTerms6.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                ibTerms6.isSelected = !ibTerms6.isSelected

                checkAllAccept()
            }

        })


        tvTermsTitle2.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(tvTermsTitle2.text.contains(term.title)){
                        startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }}

        })

        tvTermsTitle3.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(tvTermsTitle3.text.contains(term.title)){
                        if(term.isRequired)
                            startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }
        })

        tvTermsTitle4.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(tvTermsTitle4.text.contains(term.title)){
                        if(!term.isRequired)
                            startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }
        })


        tvTermsTitle5.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(tvTermsTitle5.text.contains(term.title)){
                        startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }
        })

        tvTermsTitle6.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                for(term in termsSummaryResponse){
                    if(tvTermsTitle6.text.contains(term.title)){
                        startActivity(Intent(this@TermsOfUseActivity, TermsDetailActivity::class.java).putExtra("id",term.id).putExtra("title",term.title))
                    }
                }
            }
        })
    }

    private fun checkAllAccept(){
        btnNext.isSelected = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected && ibTerms6.isSelected
        btnNext.isClickable = ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected && ibTerms6.isSelected

        if(ibTerms1.isSelected && ibTerms2.isSelected && ibTerms3.isSelected && ibTerms4.isSelected && ibTerms5.isSelected && ibTerms6.isSelected){
            ibAllAccept.isSelected = true
        }else{
            ibAllAccept.isSelected = false
        }
    }
}