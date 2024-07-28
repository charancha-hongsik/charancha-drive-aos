package com.milelog.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View.*
import android.webkit.*
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.SignInRequest
import com.milelog.retrofit.request.SignUpRequest
import com.milelog.retrofit.response.GetMyCarInfoResponse
import com.milelog.retrofit.response.SignInResponse
import com.milelog.retrofit.response.TermsAgreeStatusResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

/**
 * 1. 회원가입 및 로그인 시
 * - 약관 허용 X -> 약관 화면으로 이동
 * - Permission X -> 퍼미션 화면으로 이동
 * - 차량등록 X -> onBoarding 화면으로 이동
 * - 위 사항 모두 완료된 사용자일 경우 -> Main 화면으로 이동
 */
class LoginActivity: BaseActivity() {
    lateinit var wv_login:WebView

    val loginUrl = BuildConfig.BASE_LOGIN_URL

    /**
     * 구글 로그인 관련
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setWebview()
    }


    fun setWebview(){
        wv_login = findViewById(R.id.wv_login)
        wv_login.visibility = VISIBLE
        wv_login.settings.loadWithOverviewMode = true // 화면에 맞게 WebView 사이즈를 정의
        wv_login.settings.useWideViewPort = true //html 컨텐츠가 웹뷰에 맞게 나타나도록 합니다.
        wv_login.settings.defaultTextEncodingName = "UTF-8" // TextEncoding 이름 정의
        wv_login.settings.javaScriptEnabled = true
        wv_login.settings.userAgentString = "Mozilla/5.0 AppleWebKit/535.19 Chrome/56.0.0 Mobile Safari/535.19"
        wv_login.settings.domStorageEnabled = true
        wv_login.settings.cacheMode = WebSettings.LOAD_DEFAULT
        wv_login.settings.textZoom = 100 // System 텍스트 사이즈 변경되지 않게



        //chrome inspect 디버깅 모드
        WebView.setWebContentsDebuggingEnabled(true)

        // javascriptInterface 설정
        wv_login.addJavascriptInterface(MilelogPublicApi(this), "MilelogPublicApi")

        wv_login.webChromeClient = object:WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }

        wv_login.webViewClient = object:WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                syncCookie()
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        wv_login.loadUrl(loginUrl)

        // 쿠키 설정
        syncCookie()
    }


    /**
     * 1. webview (O)
     * 2. GA (O)
     * 3. 차량등록/지표상세 ->
     * - AOS -> 차량등록 QA중 / 지표상세 (X)
     * - iOS -> 차량등록 작업중(월요일 QA 요청)
     *
     * 4. 홈/관리점수/공통 모달창 UI 작업
     * - AOS -> 홈 데이터 없는 부분 / Permission 뜨는 부분 로직 완료 , 관리점수/모달창 UI 작업(X)
     *  -iOS -> 홈 데이터 없는 부분 / Permission 뜨는 부분 로직 완료 , 관리점수/모달창 UI 작업(X)
     */

    private fun syncCookie(){
        wv_login.settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv_login, true)
        cookieManager.flush()
    }

    class MilelogPublicApi(val activity: LoginActivity) {
        @JavascriptInterface
        fun successLogin(keylessAccount: String, keylessAccountExpire:String, oauthProvider:String, idToken:String, accountAddress:String) {
            PreferenceUtil.putPref(activity, PreferenceUtil.KEYLESS_ACCOUNT, keylessAccount)
            PreferenceUtil.putPref(activity, PreferenceUtil.KEYLESS_ACCOUNT_EXPIRE, keylessAccountExpire)
            PreferenceUtil.putPref(activity, PreferenceUtil.OAUTH_PROVIDER, oauthProvider)
            PreferenceUtil.putPref(activity, PreferenceUtil.ID_TOKEN, idToken)
            PreferenceUtil.putPref(activity, PreferenceUtil.ACCOUNT_ADDRESS, accountAddress)

            activity.handleSuccessLogin(idToken,oauthProvider.uppercase(),accountAddress)
        }

        @JavascriptInterface
        fun failLogin(message: String) {
            activity.handleFailLogin(message)
        }

    }


    fun handleSuccessLogin(idToken:String, oauthProvider:String, accountAddress:String) {
        try {
            val gson = Gson()
            val jsonParam =
                gson.toJson(SignUpRequest(idToken, "string", oauthProvider, "string", accountAddress))


            apiService().postSignUp(jsonParam.toRequestBody("application/json".toMediaTypeOrNull()))
                .enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.code() == 201 || response.code() == 409) {
                            val gson = Gson()
                            val jsonParam =
                                gson.toJson(SignInRequest(idToken, "string", oauthProvider))

                            apiService().postSignIn(jsonParam.toRequestBody("application/json".toMediaTypeOrNull()))
                                .enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {

                                        val signInResponse = gson.fromJson(
                                            response.body()?.string(),
                                            SignInResponse::class.java
                                        )

                                        PreferenceUtil.putPref(
                                            this@LoginActivity,
                                            PreferenceUtil.ACCESS_TOKEN,
                                            signInResponse.access_token
                                        )
                                        PreferenceUtil.putPref(
                                            this@LoginActivity,
                                            PreferenceUtil.REFRESH_TOKEN,
                                            signInResponse.refresh_token
                                        )
                                        PreferenceUtil.putPref(
                                            this@LoginActivity,
                                            PreferenceUtil.EXPIRES_IN,
                                            signInResponse.expires_in
                                        )
                                        PreferenceUtil.putPref(
                                            this@LoginActivity,
                                            PreferenceUtil.REFRESH_EXPIRES_IN,
                                            signInResponse.refresh_expires_in
                                        )
                                        PreferenceUtil.putPref(
                                            this@LoginActivity,
                                            PreferenceUtil.TOKEN_TYPE,
                                            signInResponse.token_type
                                        )

                                        apiService().getTermsAgree(
                                            "Bearer " + signInResponse.access_token,
                                            "MILELOG_USAGE"
                                        ).enqueue(object : Callback<ResponseBody> {
                                            override fun onResponse(
                                                call: Call<ResponseBody>,
                                                response: Response<ResponseBody>
                                            ) {

                                                if (response.code() == 200 || response.code() == 201) {
                                                    val jsonString = response.body()?.string()

                                                    val type: Type = object : TypeToken<List<TermsAgreeStatusResponse?>?>() {}.type
                                                    val termsAgreeStatusResponses:List<TermsAgreeStatusResponse> = Gson().fromJson(jsonString, type)

                                                    var agree = true

                                                    if(termsAgreeStatusResponses.isEmpty()){
                                                        startActivity(
                                                            Intent(
                                                                this@LoginActivity,
                                                                TermsOfUseActivity::class.java
                                                            )
                                                        )

                                                        wv_login.clearHistory();
                                                        wv_login.clearCache(true);
                                                        wv_login.removeJavascriptInterface("MilelogPublicApi")

                                                        finish()

                                                    }else{
                                                        for(term in termsAgreeStatusResponses){
                                                            if(term.terms.isRequired == 1)
                                                                if(term.terms.isActive == 0)
                                                                    agree = false
                                                        }
                                                        if (agree) {
                                                            if (!PreferenceUtil.getBooleanPref(
                                                                    this@LoginActivity,
                                                                    PreferenceUtil.PERMISSION_ALL_CHECKED,
                                                                    false
                                                                )
                                                            ) {
                                                                startActivity(
                                                                    Intent(
                                                                        this@LoginActivity,
                                                                        PermissionInfoActivity::class.java
                                                                    )
                                                                )

                                                                wv_login.clearHistory();
                                                                wv_login.clearCache(true);
                                                                wv_login.removeJavascriptInterface("MilelogPublicApi")
                                                                finish()
                                                            } else {
                                                                apiService().getMyCarInfo("Bearer " + signInResponse.access_token).enqueue(object :Callback<ResponseBody>{
                                                                    override fun onResponse(
                                                                        call: Call<ResponseBody>,
                                                                        response: Response<ResponseBody>
                                                                    ) {
                                                                        if(response.code() == 200 || response.code() == 201){
                                                                            val jsonString = response.body()?.string()

                                                                            val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                                                                            val getMyCarInfoResponse:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

                                                                            if(getMyCarInfoResponse.size > 0){
                                                                                PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.USER_CARID, getMyCarInfoResponse.get(0).id)
                                                                                startActivity(Intent(this@LoginActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

                                                                                wv_login.clearHistory();
                                                                                wv_login.clearCache(true);
                                                                                wv_login.removeJavascriptInterface("MilelogPublicApi")

                                                                                finish()
                                                                            }else{
                                                                                startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

                                                                                wv_login.clearHistory();
                                                                                wv_login.clearCache(true);
                                                                                wv_login.removeJavascriptInterface("MilelogPublicApi")

                                                                                finish()
                                                                            }
                                                                        }else{
                                                                            startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

                                                                            wv_login.clearHistory();
                                                                            wv_login.clearCache(true);
                                                                            wv_login.removeJavascriptInterface("MilelogPublicApi")
                                                                            finish()
                                                                        }
                                                                    }

                                                                    override fun onFailure(
                                                                        call: Call<ResponseBody>,
                                                                        t: Throwable
                                                                    ) {
                                                                        startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

                                                                        wv_login.clearHistory();
                                                                        wv_login.clearCache(true);
                                                                        wv_login.removeJavascriptInterface("MilelogPublicApi")
                                                                        finish()
                                                                    }
                                                                })
                                                            }
                                                        } else {
                                                            startActivity(
                                                                Intent(
                                                                    this@LoginActivity,
                                                                    TermsOfUseActivity::class.java
                                                                )
                                                            )

                                                            wv_login.clearHistory();
                                                            wv_login.clearCache(true);
                                                            wv_login.removeJavascriptInterface("MilelogPublicApi")

                                                            finish()

                                                        }
                                                    }
                                                } else {
                                                    startActivity(
                                                        Intent(
                                                            this@LoginActivity,
                                                            TermsOfUseActivity::class.java
                                                        )
                                                    )

                                                    wv_login.clearHistory();
                                                    wv_login.clearCache(true);
                                                    wv_login.removeJavascriptInterface("MilelogPublicApi")

                                                    finish()

                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<ResponseBody>,
                                                t: Throwable
                                            ) {

                                            }

                                        })
                                    }

                                    override fun onFailure(
                                        call: Call<ResponseBody>,
                                        t: Throwable
                                    ) {

                                    }

                                })

                        }else{

                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {


                    }
                })
        } catch (e: Exception) {

        }
    }

    fun handleFailLogin(message:String){

    }
}