package com.milelog.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.milelog.R
import com.milelog.retrofit.response.TermDetailResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

        var title = intent.getStringExtra("title")
        if(!title!!.contains("동의")){
            title = title + " 동의"
        }
        tvTermsTitle.setText(title)

    }
    
    fun setContents(){
        intent.getStringExtra("id").let{
            apiService().getTermDetails(it!!).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    try{
                        if(response.code() == 200 || response.code() == 201){
                            val gson = GsonBuilder().serializeNulls().create()
                            val termsDetailResponse = gson.fromJson(response.body()?.string(), TermDetailResponse::class.java)
                            tvTermsContents.setText(termsDetailResponse.content)
                        }else if(response.code() == 401){
                            logout()
                        }
                    }catch(e:Exception){

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