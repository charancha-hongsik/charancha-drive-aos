package com.milelog.retrofit

import okhttp3.Interceptor

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}