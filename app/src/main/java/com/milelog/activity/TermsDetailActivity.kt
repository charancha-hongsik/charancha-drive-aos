package com.milelog.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.milelog.R
import com.milelog.retrofit.response.TermDetailResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TermsDetailActivity: BaseRefreshActivity() {
    lateinit var tvTermsTitle: TextView
    lateinit var tvTermsContents: TextView
    lateinit var btnBackTerm:ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_detail)
        
        init()

    }
    
    fun init(){
        setResources()
        setContents()
        setListener()
    }
    
    fun setResources(){
        tvTermsTitle = findViewById(R.id.tv_terms_title)
        tvTermsContents = findViewById(R.id.tv_terms_contents)
        btnBackTerm = findViewById(R.id.btn_back_term)
        
        tvTermsTitle.setText(intent.getStringExtra("title"))
    }
    
    fun setContents(){
        intent.getStringExtra("id").let{
            apiService().getTermDetails(it!!).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.code() == 200 || response.code() == 201){
                        val gson = Gson()
                        val termsDetailResponse = gson.fromJson(response.body()?.string(), TermDetailResponse::class.java)
                        tvTermsContents.setText(termsDetailResponse.content)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

            })
        }
    }

    fun setListener(){
        btnBackTerm.setOnClickListener {
            finish()
        }
    }
}