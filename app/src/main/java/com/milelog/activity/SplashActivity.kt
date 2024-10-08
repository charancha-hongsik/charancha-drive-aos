package com.milelog.activity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.milelog.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.CustomDialogNoCancel
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.SignInResponse
import com.milelog.retrofit.response.TermsAgreeStatusResponse
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.SplashViewModel
import com.milelog.viewmodel.state.CheckForceUpdateState
import com.milelog.viewmodel.state.GetMyCarInfoState
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
    private val splashViewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splashViewModel.init(applicationContext)
        setObserver()
    }

    override fun onResume() {
        super.onResume()

        // 구글 스토어에 이동 후 마일로그 앱에 재진입 시 다시 체크하기 위해 onResume에 정의
        splashViewModel.checkForceUpdate()
    }

    private fun unLoginedProcess(){
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))

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
                        if(response.code() == 200 || response.code() == 201){
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
                                        var existRequired = false

                                        if(termsAgreeStatusResponses.isEmpty()){
                                            startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                                            finish()
                                        }else{
                                            for(term in termsAgreeStatusResponses){
                                                if(term.terms.isRequired == 1){
                                                    existRequired = true
                                                    if(term.isAgreed == 0)
                                                        agree = false
                                                }
                                            }

                                            if(existRequired){
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
                                                        splashViewModel.getMyCarInfo()
                                                    }
                                                } else {
                                                    startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                                                    finish()
                                                }
                                            }else{
                                                startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                                                finish()
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
                            logout()
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
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun setObserver(){
        splashViewModel.checkForceUpdate.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is CheckForceUpdateState.Loading -> {

                }
                is CheckForceUpdateState.Success -> {
                    if(state.isRequired){
                        goUpdate()
                    }else{
                        goSplash()
                    }
                }
                is CheckForceUpdateState.Error -> {

                }
                is CheckForceUpdateState.Empty -> {
                    goSplash()
                }
            }
        })

        splashViewModel.getMyCarInfo.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetMyCarInfoState.Loading -> {

                }
                is GetMyCarInfoState.Success -> {
                    if(state.data.isNotEmpty()){
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        ).putExtra("deeplink",intent.getBooleanExtra("deeplink",false)))

                        finish()
                    }else{
                        startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                        finish()
                    }
                }
                is GetMyCarInfoState.Error -> {
                    startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                    finish()
                }
                is GetMyCarInfoState.Empty -> {
                    startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                    finish()                }
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
                        "https://play.google.com/store/apps/details?id=com.milelog"
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