package com.milelog.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.webkit.*
import androidx.browser.customtabs.CustomTabsIntent
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.request.SignInRequest
import com.milelog.retrofit.request.SignUpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milelog.retrofit.request.PostConnectDeviceRequest
import com.milelog.retrofit.response.*
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

        setFcmToken(object :TokenProcessCallback{
            override fun completeProcess() {

            }
        })

//        val builder = CustomTabsIntent.Builder()
//        val customTabsIntent = builder.build()
//        customTabsIntent
//        customTabsIntent.launchUrl(this, Uri.parse("https://accounts.google.com/o/oauth2/v2/auth?client_id=261058280037-q7h5r2r541bnu4g4s29j28buapagukd5.apps.googleusercontent.com&redirect_uri=https%3A%2F%2Fdev.milelog.charancha.com%2Fcallback&response_type=id_token+token&scope=openid+email+profile&nonce=6303802816258351306621586903820938577566340891518799089734464251242280008709&prompt=select_account"))

//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl))
//        intent.setPackage("com.android.chrome")
//        startActivity(intent)


        setWebview()
    }

    /**
     * window.location.href = "intent://login?test1=hongsik1232331&test2=value2#Intent;scheme=milelog;package=com.milelog.dev;end"
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("YourActivity", "testestestsetse onNewIntent")

        intent?.let{
            handleIntent(it)
        }
    }

    private fun handleIntent(intent: Intent) {
        // 인텐트가 VIEW 액션을 가지고 있는지 확인
        if (intent.action == Intent.ACTION_VIEW) {
            // URI 가져오기
            val uri: Uri? = intent.data

            uri?.let {
                // 호스트 확인
                val host = it.host
                // 쿼리 파라미터 추출
                val test1 = it.getQueryParameter("test1")
                val test2 = it.getQueryParameter("test2")

                // test1, test2 값을 사용하여 처리
                Log.d("YourActivity", "testestestsetse testest test1: $test1, test2: $test2")

            }
        }
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
            Log.d("testestestsete","testsetestsetsetse :: " + keylessAccount)
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

        @JavascriptInterface
        fun test_login(){
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, Uri.parse("https://accounts.google.com/o/oauth2/v2/auth?client_id=261058280037-q7h5r2r541bnu4g4s29j28buapagukd5.apps.googleusercontent.com&redirect_uri=https%3A%2F%2Fdev.milelog.charancha.com%2Fcallback&response_type=id_token+token&scope=openid+email+profile&nonce=6303802816258351306621586903820938577566340891518799089734464251242280008709&prompt=select_account"))

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

                                        if (response.code() == 201) {
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

                                            PreferenceUtil.getPref(this@LoginActivity, PreferenceUtil.DEVICE_ID_FOR_FCM, "")?.let{

                                                val gson = Gson()
                                                val jsonParam =
                                                    gson.toJson(PostConnectDeviceRequest(it))


                                                apiService().postConnectDevice("Bearer " + signInResponse.access_token, makeRequestBody(jsonParam)).enqueue(object:Callback<ResponseBody>{
                                                    override fun onResponse(
                                                        call: Call<ResponseBody>,
                                                        response: Response<ResponseBody>
                                                    ) {
                                                        if(response.code() == 200 || response.code() == 201){

                                                        }
                                                        handleAfterSuccessLogin(signInResponse = signInResponse)
                                                    }

                                                    override fun onFailure(
                                                        call: Call<ResponseBody>,
                                                        t: Throwable
                                                    ) {
                                                        handleAfterSuccessLogin(signInResponse = signInResponse)
                                                    }

                                                })
                                            }?:{
                                                handleAfterSuccessLogin(signInResponse = signInResponse)
                                            }
                                        }
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

    fun handleAfterSuccessLogin(signInResponse:SignInResponse){
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

                                                finish()
                                            }else{
                                                startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

                                                finish()
                                            }
                                        }else{
                                            startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

                                            finish()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<ResponseBody>,
                                        t: Throwable
                                    ) {
                                        startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))

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

    fun handleFailLogin(message:String){

    }

    interface TokenProcessCallback {
        fun completeProcess()
    }
}