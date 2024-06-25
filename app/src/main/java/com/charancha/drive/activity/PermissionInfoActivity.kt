package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PermissionInfoActivity: BaseActivity(){
    lateinit var layoutPermissionInfoConfirm:ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_info)

        setResource()
        setListener()
    }

    fun setResource(){
        layoutPermissionInfoConfirm = findViewById(R.id.layout_permission_info_confirm)
    }

    fun setListener(){
        layoutPermissionInfoConfirm.setOnClickListener {
            startActivity(Intent(this@PermissionInfoActivity, PermissionActivity::class.java))
            finish()
        }
    }

}