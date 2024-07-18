package com.charancha.drive.activity

import android.os.Bundle
import android.widget.ImageView
import com.charancha.drive.R

class NotificationActivity:BaseRefreshActivity() {
    lateinit var btn_all_noti: ImageView
    lateinit var btn_drive_history: ImageView
    lateinit var btn_marketing: ImageView
    lateinit var btn_announcement:ImageView
    lateinit var btn_back:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_notification)

        init()
    }

    fun init(){
        btn_all_noti = findViewById(R.id.btn_all_noti)
        btn_drive_history = findViewById(R.id.btn_drive_history)
        btn_marketing = findViewById(R.id.btn_marketing)
        btn_announcement = findViewById(R.id.btn_announcement)
        btn_back = findViewById(R.id.btn_back)

        btn_all_noti.setOnClickListener {
            if(btn_all_noti.isSelected){
                btn_all_noti.isSelected = false
                btn_drive_history.isSelected = false
                btn_marketing.isSelected = false
                btn_announcement.isSelected = false
            }else{
                btn_all_noti.isSelected = true
                btn_drive_history.isSelected = true
                btn_marketing.isSelected = true
                btn_announcement.isSelected = true
            }

        }

        btn_drive_history.setOnClickListener {
            if(btn_drive_history.isSelected){
                btn_all_noti.isSelected = false
                btn_drive_history.isSelected = false
            }else{
                if(btn_marketing.isSelected && btn_announcement.isSelected){
                    btn_all_noti.isSelected = true
                }

                btn_drive_history.isSelected = true

            }

        }

        btn_marketing.setOnClickListener {
            if(btn_marketing.isSelected){
                btn_all_noti.isSelected = false
                btn_marketing.isSelected = false
            }else{
                if(btn_drive_history.isSelected && btn_announcement.isSelected){
                    btn_all_noti.isSelected = true
                }

                btn_marketing.isSelected = true
            }

        }

        btn_announcement.setOnClickListener {
            if(btn_announcement.isSelected){
                btn_all_noti.isSelected = false
                btn_announcement.isSelected = false
            }else{
                if(btn_drive_history.isSelected && btn_marketing.isSelected){
                    btn_all_noti.isSelected = true
                }

                btn_announcement.isSelected = true
            }


        }

        btn_back.setOnClickListener {
            finish()
        }
    }
}