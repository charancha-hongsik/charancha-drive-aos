package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import com.charancha.drive.retrofit.response.SignInResponse
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceUtil.getPref(this, PreferenceUtil.REFRESH_TOKEN, "") == ""){
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
//                PreferenceUtil.getPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
//                    ?.let {
//                        val gson = Gson()
//
//                        apiService().postReissue(it).enqueue(object :
//                            Callback<ResponseBody> {
//                            override fun onResponse(
//                                call: Call<ResponseBody>,
//                                response: Response<ResponseBody>
//                            ) {
//                                if(response.code() == 201){
//                                    val signInResponse = gson.fromJson(response.body()?.string(), SignInResponse::class.java)
//
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, signInResponse.access_token)
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, signInResponse.refresh_token)
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, signInResponse.expires_in)
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, signInResponse.refresh_expires_in)
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, signInResponse.token_type)
//
//                                    startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
//                                    finish()
//                                }else{
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, "")
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, "")
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
//                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, "")
//
//                                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
//                                    finish()
//                                }
//                            }
//
//                            override fun onFailure(
//                                call: Call<ResponseBody>,
//                                t: Throwable
//                            ) {
//                                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
//                                finish()
//                            }
//
//                        })
//
//
//                    }?: run{
//                    startActivity(Intent(this, LoginActivity::class.java))
//                    finish()
//                }
            }
        }, 2000) // 2000 밀리초 (2초)
    }

    fun apiService(): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl("http://172.16.10.111:3000/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }
}