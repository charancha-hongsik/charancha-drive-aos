package com.charancha.drive.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class BaseActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

}