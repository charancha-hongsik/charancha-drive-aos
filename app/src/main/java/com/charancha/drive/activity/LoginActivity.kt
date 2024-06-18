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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

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

                            Log.d("testsetsetest","testestsetsetset googleIdTokenCredential :: " + googleIdTokenCredential.idToken)
                            Toast.makeText(this@LoginActivity, "googleIdTokenCredential :: " + googleIdTokenCredential.idToken, Toast.LENGTH_SHORT).show()

                            if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.HAVE_BEEN_HOME, false)){
                                startActivity(Intent(this, MainActivity::class.java))
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
            Log.d("testsetsetest","testestestseset onFailure:: " + it.message)
            Toast.makeText(this@LoginActivity, "onFailure " ,  Toast.LENGTH_SHORT).show()
        }
    }
}