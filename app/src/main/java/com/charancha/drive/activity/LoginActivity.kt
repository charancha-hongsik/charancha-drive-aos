package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import com.charancha.drive.retrofit.request.SignInRequest
import com.charancha.drive.retrofit.request.SignUpRequest
import com.charancha.drive.retrofit.response.SignInResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

import com.google.gson.Gson
import kotlinx.coroutines.launch
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

class LoginActivity: BaseActivity() {
    lateinit var constraintLayout: ConstraintLayout

    /**
     * 구글 로그인 관
     */

    val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setBtn()

    }

    fun setBtn(){
        constraintLayout = findViewById(R.id.layout_google_login)
        constraintLayout.setOnClickListener {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("181313354113-e6ilqvbn5nsgeaobtdip5utv3pi9pvoq.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val mGoogleSignInClient = this.let { GoogleSignIn.getClient(it, gso) }

            mGoogleSignInClient.signOut()
            val signInIntent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)

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
                        if(response.code() == 201 || response.code() == 404){
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

                                    if(PreferenceUtil.getBooleanPref(this@LoginActivity, PreferenceUtil.HAVE_BEEN_HOME, false)){
                                        startActivity(Intent(this@LoginActivity, TermsOfUseActivity::class.java))
                                        finish()
                                    }else{
                                        startActivity(Intent(this@LoginActivity, TermsOfUseActivity::class.java))
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

}