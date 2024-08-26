package com.milelog.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.SignInResponse
import com.google.gson.Gson
import com.milelog.retrofit.ApiServiceInterface
import okhttp3.Interceptor
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


open class BaseRefreshActivity: BaseActivity(){
    override fun onResume() {
        super.onResume()
        postReissueAndCall()
    }

    fun postReissueAndCall(){
        PreferenceUtil.getPref(this@BaseRefreshActivity, PreferenceUtil.REFRESH_TOKEN, "")?.let{
            apiService().postReissue(PreferenceUtil.getPref(this, PreferenceUtil.REFRESH_TOKEN, "")!!).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        if (response.code() == 200 || response.code() == 201) {
                            val signInResponse =
                                Gson().fromJson(
                                    response.body()?.string(),
                                    SignInResponse::class.java
                                )

                            PreferenceUtil.putPref(
                                this@BaseRefreshActivity,
                                PreferenceUtil.ACCESS_TOKEN,
                                signInResponse.access_token
                            )
                            PreferenceUtil.putPref(
                                this@BaseRefreshActivity,
                                PreferenceUtil.REFRESH_TOKEN,
                                signInResponse.refresh_token
                            )
                            PreferenceUtil.putPref(
                                this@BaseRefreshActivity,
                                PreferenceUtil.EXPIRES_IN,
                                signInResponse.expires_in
                            )
                            PreferenceUtil.putPref(
                                this@BaseRefreshActivity,
                                PreferenceUtil.REFRESH_EXPIRES_IN,
                                signInResponse.refresh_expires_in
                            )
                            PreferenceUtil.putPref(
                                this@BaseRefreshActivity,
                                PreferenceUtil.TOKEN_TYPE,
                                signInResponse.token_type
                            )
                        } else {
                            logout()

                            startActivity(
                                Intent(
                                    this@BaseRefreshActivity,
                                    LoginActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )

                            finish()
                        }
                    }catch (e:Exception){

                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.ACCESS_TOKEN, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.REFRESH_TOKEN, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.EXPIRES_IN, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.TOKEN_TYPE, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.KEYLESS_ACCOUNT, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.KEYLESS_ACCOUNT_EXPIRE, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.OAUTH_PROVIDER, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.ID_TOKEN, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.ACCOUNT_ADDRESS, "")
                    PreferenceUtil.putPref(this@BaseRefreshActivity, PreferenceUtil.USER_CARID, "")
                    PreferenceUtil.putBooleanPref(this@BaseRefreshActivity, PreferenceUtil.HAVE_BEEN_HOME, false)

                    startActivity(Intent(this@BaseRefreshActivity, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }

            })
        }?: run{
            startActivity(Intent(this@BaseRefreshActivity, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }

    class AuthInterceptor(
        private val apiService: ApiServiceInterface // 토큰 갱신을 위한 ApiService
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var request = chain.request()
            var response = chain.proceed(request)

            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            // 401 Unauthorized 에러가 발생한 경우
//            if (response.code == 401) {
//                synchronized(this) {
//                    // 토큰 갱신을 시도합니다.
//                    val newTokenResponse = apiService.postReissue().execute()
//                    if (newTokenResponse.isSuccessful) {
//                        // 새 토큰을 저장합니다.
//                        val newToken = newTokenResponse.body()?.token
////                        prefs.edit().putString("access_token", newToken).apply()
//
//                        // 원래 요청에 새 토큰을 추가하여 재시도합니다.
//                        val newRequest = request.newBuilder()
//                            .header("Authorization", "Bearer $newToken")
//                            .build()
//
//                        response.close() // 이전 응답 닫기
//                        response = chain.proceed(newRequest) // 새 요청을 수행
//                    }
//                }
//            }

            Log.d("testsetestest","testsetestes request.url :: " + request.url)

            Log.d("testsetestest","testsetestes AuthInterceptor :: " + response.code)

            return response
        }
    }

}
