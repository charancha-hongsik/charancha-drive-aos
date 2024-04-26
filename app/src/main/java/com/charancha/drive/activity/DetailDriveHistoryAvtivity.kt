package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R
import com.charancha.drive.room.entity.Drive
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import com.google.gson.Gson


class DetailDriveHistoryAvtivity: AppCompatActivity() {
    lateinit var tvTrackingId:TextView
    lateinit var tvTimestamp:TextView
    lateinit var tvRank:TextView
    lateinit var tvDistance:TextView
    lateinit var tvTime:TextView
    lateinit var tvRapid1:TextView
    lateinit var tvRapid2:TextView

    lateinit var drive:Drive


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_drive_history)

        init()
    }

    private fun init(){
        tvTrackingId = findViewById(R.id.tv_tracking_id)
        tvTimestamp = findViewById(R.id.tv_timestamp)
        tvRank = findViewById(R.id.tv_rank)
        tvDistance = findViewById(R.id.tv_distance)
        tvTime = findViewById(R.id.tv_time)
        tvRapid1 = findViewById(R.id.tv_rapid1)
        tvRapid2 = findViewById(R.id.tv_rapid2)

        drive = Gson().fromJson(intent.getStringExtra("drive"), Drive::class.java)

        tvTrackingId.text = "id : " + drive.tracking_id
        tvTimestamp.text = "주행시작 : " + drive.timeStamp
        tvRank.text = "랭크 : " + drive.rank
        tvDistance.text = "주행거리(m) : " + drive.distance
        tvTime.text = "주행 시간 : " + drive.time
        tvRapid1.text = "주행 종료 : " + (drive.timeStamp + drive.time)



    }
}