package com.charancha.drive.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.ApiServiceInterface2
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import com.charancha.drive.retrofit.request.SignInRequest
import com.charancha.drive.retrofit.request.SignUpRequest
import com.charancha.drive.retrofit.request.keylessAccountRequest
import com.charancha.drive.retrofit.response.SignInResponse
import com.charancha.drive.retrofit.response.TermsAgreeStatusResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 1. 회원가입 및 로그인 시
 * - 약관 허용 X -> 약관 화면으로 이동
 * - Permission X -> 퍼미션 화면으로 이동
 * - 차량등록 X -> onBoarding 화면으로 이동
 * - 위 사항 모두 완료된 사용자일 경우 -> Main 화면으로 이동
 */
class LoginActivity: BaseActivity() {
    lateinit var constraintLayout: ConstraintLayout
    lateinit var wv_login:WebView

    val loginUrl = "https://0565-222-109-154-193.ngrok-free.app/"

    /**
     * 구글 로그인 관련
     */

    val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setBtn()
//        setWebview()
    }

    fun setBtn(){
        constraintLayout = findViewById(R.id.layout_google_login)
        constraintLayout.setOnClickListener {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("181313354113-e6ilqvbn5nsgeaobtdip5utv3pi9pvoq.apps.googleusercontent.com")
//                .requestIdToken("345319283419-7u6i45h9b8n575mulpb6dkb17d8bgr8k.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val mGoogleSignInClient = this.let { GoogleSignIn.getClient(it, gso) }

            mGoogleSignInClient.signOut()
            val signInIntent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)
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

        Log.d("testestset","testestsetesest webview :: ")

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

    class MilelogPublicApi(val activity:LoginActivity) {
        @JavascriptInterface
        fun successLogin(keylessAccount: String, keylessAccountExpire:String, oauthProvider:String) {
            Log.d("testestset","testestsetesest keylessAccount :: " + keylessAccount)
            Log.d("testestset","testestsetesest keylessAccountExpire :: " + keylessAccountExpire)
            Log.d("testestset","testestsetesest oauthProvider :: " + oauthProvider)

            val gson = Gson()
            val jsonParam =
                gson.toJson(keylessAccountRequest(keylessAccount))

            activity.apiService2().postDrivingInfo(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d("testestset","testestsetesest response :: " + response.code())
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })

        }

        @JavascriptInterface
        fun failLogin(message: String) {
            activity.handleFailLogin(message)
        }

    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == -1) {

            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    fun handleSuccessLogin(idToken:String) {
        try {
            val gson = Gson()
            val jsonParam =
                gson.toJson(SignUpRequest(idToken, "string", "GOOGLE", "string"))

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
                                gson.toJson(SignInRequest(idToken, "string", "GOOGLE"))


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
                                            "마일로그_서비스",
                                            true
                                        ).enqueue(object : Callback<ResponseBody> {
                                            override fun onResponse(
                                                call: Call<ResponseBody>,
                                                response: Response<ResponseBody>
                                            ) {
                                                if (response.code() == 200 || response.code() == 201) {
                                                    val termsAgreeStatusResponse = gson.fromJson(
                                                        response.body()?.string(),
                                                        TermsAgreeStatusResponse::class.java
                                                    )
                                                    if (termsAgreeStatusResponse.agreed) {
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
                                                            startActivity(
                                                                Intent(
                                                                    this@LoginActivity,
                                                                    OnBoardingActivity::class.java
                                                                )
                                                            )
                                                            finish()
                                                        }
                                                    } else {
                                                        startActivity(
                                                            Intent(
                                                                this@LoginActivity,
                                                                TermsOfUseActivity::class.java
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    startActivity(
                                                        Intent(
                                                            this@LoginActivity,
                                                            TermsOfUseActivity::class.java
                                                        )
                                                    )
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<ResponseBody>,
                                                t: Throwable
                                            ) {
                                                TODO("Not yet implemented")
                                            }

                                        })
                                    }

                                    override fun onFailure(
                                        call: Call<ResponseBody>,
                                        t: Throwable
                                    ) {

                                    }

                                })

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

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try {

            val account = completedTask.getResult(ApiException::class.java)
            try {
            // Use googleIdTokenCredential and extract id to validate and
            // authenticate on your server.

                Log.d("testestsetset","testsetsetsetset idToken:: " + account.idToken!!)


                val gson = Gson()
                val jsonParam = gson.toJson(SignUpRequest(account.idToken!!, "string", "GOOGLE","string"))

                apiService().postSignUp(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                    Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(response.code() == 201 || response.code() == 409){
                            val gson = Gson()
                            val jsonParam = gson.toJson(SignInRequest(account.idToken!!, "string","GOOGLE"))


                            apiService().postSignIn(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object : Callback<ResponseBody>{
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>
                                ) {

                                    val signInResponse = gson.fromJson(response.body()?.string(), SignInResponse::class.java)

                                    PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.ACCESS_TOKEN, signInResponse.access_token)
                                    PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.REFRESH_TOKEN, signInResponse.refresh_token)
                                    PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.EXPIRES_IN, signInResponse.expires_in)
                                    PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.REFRESH_EXPIRES_IN, signInResponse.refresh_expires_in)
                                    PreferenceUtil.putPref(this@LoginActivity, PreferenceUtil.TOKEN_TYPE, signInResponse.token_type)

                                    apiService().getTermsAgree("Bearer " + signInResponse.access_token, "마일로그_서비스", true).enqueue(object :Callback<ResponseBody>{
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            if(response.code() == 200 || response.code() == 201){
                                                val termsAgreeStatusResponse = gson.fromJson(response.body()?.string(), TermsAgreeStatusResponse::class.java)
                                                if(termsAgreeStatusResponse.agreed){
                                                    if(!PreferenceUtil.getBooleanPref(this@LoginActivity, PreferenceUtil.PERMISSION_ALL_CHECKED, false)){
                                                        startActivity(Intent(this@LoginActivity, PermissionInfoActivity::class.java))
                                                        finish()
                                                    }else{
                                                        startActivity(Intent(this@LoginActivity, OnBoardingActivity::class.java))
                                                        finish()
                                                    }
                                                }else{
                                                    startActivity(Intent(this@LoginActivity, TermsOfUseActivity::class.java))
                                                }
                                            }else{
                                                startActivity(Intent(this@LoginActivity, TermsOfUseActivity::class.java))
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<ResponseBody>,
                                            t: Throwable
                                        ) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }

                                override fun onFailure(
                                    call: Call<ResponseBody>,
                                    t: Throwable
                                ) {

                                }

                            })

                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {


                    }
                })
        } catch (e: Exception) {

        }

        } catch (e: ApiException){
            Log.w("failed", "signInResult:failed code=" + e.statusCode)
        }
    }

    fun apiService2(): ApiServiceInterface2 {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl("https://93e0-222-109-154-193.ngrok-free.app/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface2::class.java
            )
    }

}