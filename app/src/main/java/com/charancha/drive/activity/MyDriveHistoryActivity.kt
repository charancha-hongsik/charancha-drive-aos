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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MyDriveHistoryActivity: BaseRefreshActivity() {
    lateinit var lvHistory:ListView
    lateinit var btn_filter: ImageView
    lateinit var btn_back:ImageView

    private val historyViewModel: MyDriveHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_history)

        init()

        historyViewModel.init(applicationContext)
        historyViewModel.getAllDrive()
    }

    fun init(){
        lvHistory = findViewById(R.id.lv_history)
        btn_filter = findViewById(R.id.btn_filter)
        btn_back = findViewById(R.id.btn_back)

        btn_back.setOnClickListener {
            finish()
        }


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
            val tv_start_time = listItemView!!.findViewById<TextView>(R.id.tv_start_time)
            val tv_end_time = listItemView!!.findViewById<TextView>(R.id.tv_end_time)


            tvDate.text = driveItem?.createdAt
            tv_distance.text = transferDistanceWithUnit(driveItem?.totalDistance!!, PreferenceUtil.getPref(context,  PreferenceUtil.KM_MILE, "km")!!)
            tv_start_time.text = transformTimeToHHMM(driveItem?.startTime!!)
            tv_end_time.text = transformTimeToHHMM(driveItem?.endTime!!)



            btn_drive_history.setOnClickListener {
                var intent = Intent(context, DetailDriveHistoryActivity::class.java)
                intent.putExtra("tracking_id", driveItem?.id)
                context.startActivity(intent)
            }

            return listItemView
        }

        fun transferDistanceWithUnit(meters:Double, distance_unit:String):String{
            if(distance_unit == "km"){
                return String.format(Locale.KOREAN, "%.3fkm", meters / 1000)
            }else{
                val milesPerMeter = 0.000621371
                return String.format(Locale.KOREAN, "%.3fmile",meters * milesPerMeter)
            }
        }

        private fun transformTimeToHHMM(isoDate: String):String{
            // ISO 8601 형식의 날짜 문자열을 ZonedDateTime 객체로 변환
            val zonedDateTime = ZonedDateTime.parse(isoDate)

            // 원하는 형식의 DateTimeFormatter 생성
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)

            // 포맷된 문자열 반환
            return zonedDateTime.format(formatter)
        }

    }
}