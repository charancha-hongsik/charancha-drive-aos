package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R

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
        layoutPermissionInfoConfirm.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@PermissionInfoActivity, PermissionActivity::class.java))
                finish()
            }

        })
    }

}