package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
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
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

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
                    if(response.code() == 201) {
                        val signInResponse =
                            Gson().fromJson(response.body()?.string(), SignInResponse::class.java)

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
                    }else{
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

                        startActivity(Intent(this@BaseRefreshActivity, LoginActivity::class.java))
                        finish()
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

                    startActivity(Intent(this@BaseRefreshActivity, LoginActivity::class.java))
                    finish()                }

            })
        }?: run{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
