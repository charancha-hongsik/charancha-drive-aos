package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.retrofit.response.SignInResponse
import com.charancha.drive.retrofit.response.TermsAgreeStatusResponse
import com.charancha.drive.retrofit.response.TermsSummaryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

/**
 * 1. 로그인 되어있는지 체크 (RefreshToken)
 *  비로그인 유저(토큰 없는 유저 / 토큰이 만료된 유저) -> 로그인 창으로 이동
 *  로그인 유저 -> 토큰 갱신
 *
 * 2. 로그인 유저는 아래 사항 체크
 * - 약관 허용을 X -> 로그아웃 / 로그인 화면으로 이동
 * - Permission X -> 퍼미션 화면으로 이동
 * - 차량등록 X -> onBoarding 화면으로 이동
 * - 위 사항 모두 완료된 사용자일 경우 -> Main 화면으로 이동
 */
class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceUtil.getPref(this, PreferenceUtil.REFRESH_TOKEN, "") == ""){
                unLoginedProcess()
            }else {
                loginedProcess()
            }
        }, 2000) // 2000 밀리초 (2초)
    }

    private fun unLoginedProcess(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loginedProcess(){
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
                                            if(!PreferenceUtil.getBooleanPref(this@SplashActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                                                startActivity(Intent(this@SplashActivity, PermissionInfoActivity::class.java))
                                                finish()
                                            }else{
                                                apiService().getMyCarInfo("Bearer " + signInResponse.access_token).enqueue(object :Callback<ResponseBody>{
                                                    override fun onResponse(
                                                        call: Call<ResponseBody>,
                                                        response: Response<ResponseBody>
                                                    ) {
                                                        if(response.code() == 200){
                                                            val jsonString = response.body()?.string()

                                                            val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                                                            val getMyCarInfoResponse:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

                                                            if(getMyCarInfoResponse.size > 0){
                                                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                                                finish()
                                                            }else{
                                                                startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                                                                finish()
                                                            }
                                                        }else{
                                                            startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                                                            finish()
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<ResponseBody>,
                                                        t: Throwable
                                                    ) {
                                                        startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                                                        finish()
                                                    }
                                                })
                                            }
                                        }else{
                                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, "")
                                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
                                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, "")
                                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
                                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, "")
                                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
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
}