package com.charancha.drive.activity

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.charancha.drive.CustomDialog
import com.charancha.drive.CustomDialogNoCancel
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.*
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
    }

    override fun onResume() {
        super.onResume()
        checkForceUpdate()
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


                            apiService().getTermsAgree("Bearer " + PreferenceUtil.getPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, ""), "MILELOG_USAGE").enqueue(object :Callback<ResponseBody>{
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>
                                ) {
                                    if(response.code() == 200 || response.code() == 201){
                                        val jsonString = response.body()?.string()

                                        val type: Type = object : TypeToken<List<TermsAgreeStatusResponse?>?>() {}.type
                                        val termsAgreeStatusResponses:List<TermsAgreeStatusResponse> = Gson().fromJson(jsonString, type)

                                        var agree = true

                                        if(termsAgreeStatusResponses.isEmpty()){
                                            startActivity(
                                                Intent(
                                                    this@SplashActivity,
                                                    TermsOfUseActivity::class.java
                                                )
                                            )
                                        }else{
                                            for(term in termsAgreeStatusResponses){
                                                if(term.terms.isRequired == 1)
                                                    if(term.terms.isActive == 0)
                                                        agree = false
                                            }
                                            if (agree) {
                                                if (!PreferenceUtil.getBooleanPref(
                                                        this@SplashActivity,
                                                        PreferenceUtil.PERMISSION_ALL_CHECKED,
                                                        false
                                                    )
                                                ) {
                                                    startActivity(
                                                        Intent(
                                                            this@SplashActivity,
                                                            PermissionInfoActivity::class.java
                                                        )
                                                    )
                                                    finish()
                                                } else {
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
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        this@SplashActivity,
                                                        TermsOfUseActivity::class.java
                                                    )
                                                )
                                            }
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
                                }

                            })
                        }else{
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCESS_TOKEN, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.EXPIRES_IN, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.TOKEN_TYPE, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.KEYLESS_ACCOUNT, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.KEYLESS_ACCOUNT_EXPIRE, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.OAUTH_PROVIDER, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ID_TOKEN, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.ACCOUNT_ADDRESS, "")
                            PreferenceUtil.putPref(this@SplashActivity, PreferenceUtil.USER_CARID, "")

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

    private fun checkForceUpdate(){
        apiService().getLatest("AOS","PHONE").enqueue(object :Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {
                    val getLatestResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetLatestResponse::class.java
                    )

                    try {
                        val info = packageManager.getPackageInfo(packageName, 0)
                        if (info != null && info.versionName != null) {
                            val currentAppVersion = info.versionName
                            val majorFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[0]
                            val minorFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[1]
                            val patchFromApi =
                                getLatestResponse.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[2]
                            val major = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[0]
                            val minor = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1]
                            val patch = currentAppVersion.split("\\.".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()[2]



                            if (patchFromApi.toInt() > patch.toInt()) {
                                goUpdate()
                                return
                            }
                            if (minorFromApi.toInt() > minor.toInt()) {
                                goUpdate()
                                return
                            }
                            if (majorFromApi.toInt() > major.toInt()) {
                                goUpdate()
                                return
                            }

                            goSplash()

                        } else {
                            goSplash()
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        goSplash()
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                goSplash()
            }

        })
    }

    private fun goUpdate(){
        CustomDialogNoCancel(
            this,
            "최신 버전 업데이트",
            "안정적인 서비스 이용을 위해 최신 버전 업데이트가 필요합니다.",
            "업데이트",
            object : CustomDialogNoCancel.DialogCallback {
                override fun onConfirm() {
                    val app: ApplicationInfo
                    app = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(
                            "com.android.vending",
                            PackageManager.ApplicationInfoFlags.of(0)
                        )
                    } else {
                        packageManager.getApplicationInfo(
                            "com.android.vending",
                            PackageManager.GET_META_DATA
                        )
                    }
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.charancha"
                    )
                    intent.setPackage("com.android.vending")
                    startActivity(intent)
                }
            }).show()
    }

    private fun goSplash(){
        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceUtil.getPref(this, PreferenceUtil.REFRESH_TOKEN, "") == ""){
                unLoginedProcess()
            }else {
                loginedProcess()
            }
        }, 2000) // 2000 밀리초 (2초)
    }

}