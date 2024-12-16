package com.milelog.activity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import com.milelog.*
import com.milelog.CustomDialogNoCancel
import com.milelog.PreferenceUtil
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.SplashViewModel
import com.milelog.viewmodel.state.CheckForceUpdateState
import com.milelog.viewmodel.state.GetMyCarInfoState
import com.milelog.viewmodel.state.GetTermsAgreeState
import com.milelog.viewmodel.state.PostReissueState

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

        splashViewModel.checkForceUpdate()
    }

    override fun onResume() {
        super.onResume()

        // 구글 스토어에 이동 후 마일로그 앱에 재진입 시 다시 체크하기 위해 onResume에 정의
    }

    private fun unLoginedProcess(){
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))

        finish()
    }

    private fun loginedProcess(){
        PreferenceUtil.getPref(this@SplashActivity, PreferenceUtil.REFRESH_TOKEN, "")
            ?.let {
                splashViewModel.postReissue()
            }?: run{
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun setObserver(){
        splashViewModel.checkForceUpdate.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is CheckForceUpdateState.Loading -> {
                    Log.d("testestestsetest","testestestset checkForceUpdate:: ")

                }
                is CheckForceUpdateState.Success -> {
                    Log.d("testestestsetest","testestestset checkForceUpdate:: ")

                    if(state.isRequired){
                        goUpdate()
                    }else{
                        goSplash()
                    }
                }
                is CheckForceUpdateState.Error -> {
                    Log.d("testestestsetest","testestestset checkForceUpdate:: ")
                    goSplash()
                }
                is CheckForceUpdateState.Empty -> {
                    Log.d("testestestsetest","testestestset checkForceUpdate:: ")
                    goSplash()
                }
            }
        })

        splashViewModel.getMyCarInfo.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetMyCarInfoState.Loading -> {

                }
                is GetMyCarInfoState.Success -> {
                    Log.d("testestestsetest","testestestset getMyCarInfo:: ")

                    if(state.data > 0){
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
                    if(state.code == 401){
                        logout()
                    }else{
                        startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                        finish()
                    }
                }
                is GetMyCarInfoState.Empty -> {
                    startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                    finish()
                }
            }
        })

        splashViewModel.getTermsAgree.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetTermsAgreeState.Loading -> {

                }
                is GetTermsAgreeState.Success -> {
                    Log.d("testestestsetest","testestestset getTermsAgree:: ")

                    val termsAgreeStatusResponses = state.data

                    var agree = true
                    var existRequired = false

                    if(termsAgreeStatusResponses.isEmpty()){
                        startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                        finish()
                    }else{
                        for(term in termsAgreeStatusResponses){
                            if(term.terms.isRequired){
                                existRequired = true
                                if(!term.isAgreed)
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
                }
                is GetTermsAgreeState.Error -> {
                    if(state.code == 401){
                        logout()
                    }else{
                        startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                        finish()
                    }
                }
                is GetTermsAgreeState.Empty -> {
                    startActivity(Intent(this@SplashActivity, TermsOfUseActivity::class.java))
                    finish()
                }
            }
        })

        splashViewModel.postReissue.observe(this@SplashActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is PostReissueState.Loading -> {

                }
                is PostReissueState.Success -> {
                    Log.d("testestestsetest","testestestset postReissue:: ")

                    splashViewModel.getTermsAgree()
                }
                is PostReissueState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is PostReissueState.Empty -> {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
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