package com.charancha.drive.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.room.dto.SignInDto
import com.charancha.drive.room.dto.SignUpDto
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.Interceptor
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

class LoginActivity: AppCompatActivity() {
    lateinit var constraintLayout: ConstraintLayout

    /**
     * 구글 로그인 관
     */
    lateinit var googleIdOption:GetGoogleIdOption
    lateinit var request: GetCredentialRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setBtn()

    }

    fun setBtn(){
        constraintLayout = findViewById(R.id.layout_google_login)
        constraintLayout.setOnClickListener {
            lifecycleScope.launch {
                requestGoogleLogin(this@LoginActivity)
            }
//            startActivity(Intent(this, PermissionActivity::class.java))
//            finish()

        }
    }

    suspend fun requestGoogleLogin(
        activityContext : Context,
    ) {
        googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId("181313354113-e6ilqvbn5nsgeaobtdip5utv3pi9pvoq.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()


        request =
            GetCredentialRequest.Builder().addCredentialOption(
                googleIdOption
            ).build()

        val credentialManager = CredentialManager.create(this)

        runCatching {
            credentialManager.getCredential(
                request = request,
                context = activityContext,
            )
        }.onSuccess {
            //성공시 액션
            val credential = it.credential

            when(credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Use googleIdTokenCredential and extract id to validate and
                            // authenticate on your server.
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)

                            val gson = Gson()
                            val jsonParam = gson.toJson(SignInDto(googleIdTokenCredential.idToken, "string", "GOOGLE"))



                            apiService().postSignIn(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object :
                                Callback<ResponseBody>{
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>
                                ) {
                                    val gson = Gson()
                                    val jsonParam = gson.toJson(SignUpDto(googleIdTokenCredential.idToken, "GOOGLE","01010022"))

                                    apiService().postSignIn(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object : Callback<ResponseBody>{
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {

                                        }

                                        override fun onFailure(
                                            call: Call<ResponseBody>,
                                            t: Throwable
                                        ) {

                                        }

                                    })


                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {


                                }
                            })

                            if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.HAVE_BEEN_HOME, false)){
                                startActivity(Intent(this, TermsOfUseActivity::class.java))
                                finish()
                            }else{
                                startActivity(Intent(this, TermsOfUseActivity::class.java))
                                finish()
                            }

                        } catch (e: GoogleIdTokenParsingException) {

                        }
                    }
                }
            }
        }.onFailure {


        }
    }

    fun apiService(): ApiServiceInterface {


        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl("http://172.16.10.111:3000/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }


    class HeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }

}