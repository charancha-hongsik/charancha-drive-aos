package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R
import com.charancha.drive.room.DriveDto
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import com.google.gson.Gson


class MyDriveHistoryActivity: AppCompatActivity() {
    lateinit var lvHistory:ListView
    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)



        lvHistory = findViewById(R.id.lv_history)

        historyViewModel.init(applicationContext)


        historyViewModel.setAllDriveDate.observe(this@MyDriveHistoryActivity, MyDriveHistoryViewModel.EventObserver {
            var id_list:MutableList<String> = mutableListOf()
                for(drive in it)
                    id_list.add(drive.tracking_id)

            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, id_list)

            lvHistory.setOnItemClickListener { adapterView, view, i, l ->
                var intent = Intent(this@MyDriveHistoryActivity, DetailDriveHistoryActivity::class.java)
                intent.putExtra("driveDto",Gson().fromJson(it.get(i).jsonData, DriveDto::class.java))
                startActivity(intent)
            }

            // listView에 adapter 연결
            lvHistory.setAdapter(adapter)
        })

        historyViewModel.getAllDrive()


    }
}