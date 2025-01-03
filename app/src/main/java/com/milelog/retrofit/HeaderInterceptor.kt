package com.milelog.retrofit

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.retrofit.response.SignInResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HeaderInterceptor(val context: Context, val content_type:String="application/json") : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

        val originalRequest = chain.request()

        // 요청이 이미 재시도된 것인지 확인하는 플래그
        if (originalRequest.header("isRetry") == "true") {
            return chain.proceed(originalRequest)
        }

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", content_type)
        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // 401 Unauthorized 처리
        if(!request.url.toString().contains("reissue")){
            if (response.code == 401) {
                PreferenceUtil.getPref(context, PreferenceUtil.REFRESH_TOKEN, "")?.let{
                    // Refresh token을 이용해 새로운 액세스 토큰 요청
                    val newTokenResponse = apiService().postReissue(it).execute()

                    return if (newTokenResponse.isSuccessful) {

                        response.close()

                        val signInResponse =
                            GsonBuilder().serializeNulls().create().fromJson(
                                newTokenResponse.body()?.string(),
                                SignInResponse::class.java
                            )

                        PreferenceUtil.putPref(
                            context,
                            PreferenceUtil.ACCESS_TOKEN,
                            signInResponse.access_token
                        )
                        PreferenceUtil.putPref(
                            context,
                            PreferenceUtil.REFRESH_TOKEN,
                            signInResponse.refresh_token
                        )
                        PreferenceUtil.putPref(
                            context,
                            PreferenceUtil.EXPIRES_IN,
                            signInResponse.expires_in
                        )
                        PreferenceUtil.putPref(
                            context,
                            PreferenceUtil.REFRESH_EXPIRES_IN,
                            signInResponse.refresh_expires_in
                        )
                        PreferenceUtil.putPref(
                            context,
                            PreferenceUtil.TOKEN_TYPE,
                            signInResponse.token_type
                        )

                        // 새로운 토큰으로 원래 요청 재시도
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${signInResponse.access_token}")
                            .header("isRetry", "true") // 재시도 플래그 추가
                            .build()

                        Log.d("testestsetest","testestsesetse newRequest :: " + newRequest.url)
                        val response2 = chain.proceed(newRequest)
                        Log.d("testestsetest","testestsesetse response2 :: " + response2.code)

                        // 재시도 요청 보내기
                        response2
                    } else {
                        // Refresh 실패시 원래 response 반환
                        response
                    }
                }


            }
        }

        return response
    }

    fun apiService(readTimeOut:Long = 30): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_API_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }
}