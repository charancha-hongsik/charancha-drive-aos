package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.SignInResponse
import com.charancha.drive.retrofit.response.TermsAgreeStatusResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 로그인 되어있는지 체크 (RefreshToken)
 */
class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceUtil.getPref(this, PreferenceUtil.REFRESH_TOKEN, "") == ""){
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
                PreferenceUtil.getPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
                    ?.let {
                        val gson = Gson()

                        apiService().postReissue(it).enqueue(object :
                            Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 201){
                                    val signInResponse = gson.fromJson(response.body()?.string(), SignInResponse::class.java)

                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, signInResponse.access_token)
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, signInResponse.refresh_token)
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, signInResponse.expires_in)
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, signInResponse.refresh_expires_in)
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, signInResponse.token_type)

                                    apiService().getTermsAgree("Bearer " + signInResponse.access_token, "마일로그_서비스", true).enqueue(object :Callback<ResponseBody>{
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            if(response.code() == 200 || response.code() == 201){
                                                val termsAgreeStatusResponse = gson.fromJson(response.body()?.string(), TermsAgreeStatusResponse::class.java)
                                                if(termsAgreeStatusResponse.agreed){
                                                    if(PreferenceUtil.getBooleanPref(this@SplashActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                                                        startActivity(Intent(this@SplashActivity, PermissionInfoActivity::class.java))
                                                        finish()
                                                    }else{
                                                        startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                                                        finish()
                                                    }
                                                }else{
                                                    startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                                                    finish()
                                                }
                                            }else{
                                                startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                                                finish()
                                            }

                                        }

                                        override fun onFailure(
                                            call: Call<ResponseBody>,
                                            t: Throwable
                                        ) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }else{
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, "")
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, "")
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
                                    PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, "")

                                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                                    finish()
                                }
                            }

                            override fun onFailure(
                                call: Call<ResponseBody>,
                                t: Throwable
                            ) {
                                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                                finish()
                            }

                        })


                    }?: run{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }, 2000) // 2000 밀리초 (2초)
    }
}