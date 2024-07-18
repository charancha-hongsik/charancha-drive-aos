package com.charancha.drive.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.charancha.drive.ChosenDate
import com.charancha.drive.DriveHistroyData
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.DriveItem
import com.charancha.drive.retrofit.response.GetDriveHistoryResponse
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyDriveHistoryActivity: BaseActivity() {
    lateinit var lvHistory:ListView
    lateinit var btn_filter: ImageView
    lateinit var btn_back:ImageView
    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)

        init()

    }

    fun init(){
        lvHistory = findViewById(R.id.lv_history)
        btn_filter = findViewById(R.id.btn_filter)
        btn_back = findViewById(R.id.btn_back)


        getHistories()
    }

    fun getHistories(){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            10,
            "ASC",
            null,
            null,
            "startTime",
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){

                    val getDriveHistroyResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )

                    val dateAdapter = DriveHistoryAdapter(
                        this@MyDriveHistoryActivity,
                        getDriveHistroyResponse.items)

                    lvHistory.adapter = dateAdapter
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    class DriveHistoryAdapter(context: Context, histories: List<DriveItem> ) : ArrayAdapter<DriveItem>(context, 0, histories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
            if (listItemView == null) {
                listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history, parent, false)
            }

            val driveItem = getItem(position)

            val tvDate = listItemView!!.findViewById<TextView>(R.id.tv_date)
            val tv_distance = listItemView!!.findViewById<TextView>(R.id.tv_distance)
            val btn_drive_history = listItemView!!.findViewById<ConstraintLayout>(R.id.btn_drive_history)
            tvDate.text = driveItem?.createdAt
            tv_distance.text = driveItem?.totalDistance.toString() + "m"


            btn_drive_history.setOnClickListener {
                var intent = Intent(context, DetailDriveHistoryActivity::class.java)
                intent.putExtra("tracking_id", driveItem?.id)
                context.startActivity(intent)
            }

            return listItemView
        }
    }
}