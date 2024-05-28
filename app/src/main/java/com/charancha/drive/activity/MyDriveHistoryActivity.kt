package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.R
import com.charancha.drive.calculateData
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
                for(drive in it) {
                    id_list.add(
                        drive.tracking_id.subSequence(0, 4)
                            .toString() + "-" + drive.tracking_id.subSequence(4, 8)
                    )
                }


            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, id_list)

            lvHistory.setOnItemClickListener { adapterView, view, i, l ->
                var intent = Intent(this@MyDriveHistoryActivity, DetailDriveHistoryActivity::class.java)
                intent.putExtra("tracking_id",
                    DriveDto(
                    it.get(i).tracking_id,
                    it.get(i).timeStamp,
                    it.get(i).verification,
                    it.get(i).distance_array,
                    it.get(i).time,
                    it.get(i).sudden_deceleration_array,
                    it.get(i).sudden_stop_array,
                    it.get(i).sudden_acceleration_array,
                    it.get(i).sudden_start_array,
                    it.get(i).high_speed_driving_array,
                    it.get(i).low_speed_driving_array,
                    it.get(i).constant_speed_driving_array,
                    it.get(i).harsh_driving_array,
                    it.get(i).sum_sudden_deceleration_speed,
                    listOf()
                    )
                )
                startActivity(intent)
            }

            // listView에 adapter 연결
            lvHistory.setAdapter(adapter)
        })

        historyViewModel.getAllDrive()


    }
}