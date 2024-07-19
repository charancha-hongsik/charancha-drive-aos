package com.charancha.drive.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import com.charancha.drive.retrofit.response.Meta
import com.charancha.drive.viewmodel.MyDriveHistoryViewModel
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneId
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

    fun getHistoriesMore(meta:Meta, histories: MutableList<DriveItem>){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            30,
            "DESC",
            meta.afterCursor,
            null,
            "startTime",
            "",
            "").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDriveHistroyResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )

                    histories.addAll(getDriveHistroyResponse.items)

                    histories.add(DriveItem("","","","","",false,"","",0.0,0.0))
                    meta.afterCursor = getDriveHistroyResponse.meta.afterCursor
                    (lvHistory.adapter as DriveHistoryAdapter).notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }
    fun getHistories(){
        apiService().getDrivingHistories(
            "Bearer " + PreferenceUtil.getPref(this@MyDriveHistoryActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            30,
            "DESC",
            null,
            null,
            "startTime",
            "",
            "").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDriveHistroyResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDriveHistoryResponse::class.java
                    )

                    val driveAdapter = DriveHistoryAdapter(
                        this@MyDriveHistoryActivity,
                        getDriveHistroyResponse.items, getDriveHistroyResponse.meta, object :DriveHistoryAdapter.DriveCallback{
                            override fun clickedMore(meta: Meta, histories: MutableList<DriveItem>) {
                                histories.removeLast()
                                getHistoriesMore(meta, histories)

                            }
                        })

                    getDriveHistroyResponse.items.add(DriveItem("","","","","",false,"","",0.0,0.0))

                    lvHistory.adapter = driveAdapter
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    class DriveHistoryAdapter(context: Context, val histories: MutableList<DriveItem>, var meta: Meta, val callback:DriveCallback) : ArrayAdapter<DriveItem>(context, 0, histories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            if(position != histories.size -1){
                var listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history, parent, false)


                val driveItem = getItem(position)

                val tvDate = listItemView!!.findViewById<TextView>(R.id.tv_date)
                val tv_distance = listItemView!!.findViewById<TextView>(R.id.tv_distance)
                val btn_drive_history = listItemView!!.findViewById<ConstraintLayout>(R.id.btn_drive_history)
                val tv_start_time = listItemView!!.findViewById<TextView>(R.id.tv_start_time)
                val tv_end_time = listItemView!!.findViewById<TextView>(R.id.tv_end_time)


                tvDate.text = transformTimeToDate(driveItem?.createdAt!!)
                tv_distance.text = transferDistanceWithUnit(driveItem?.totalDistance!!, PreferenceUtil.getPref(context,  PreferenceUtil.KM_MILE, "km")!!)
                tv_start_time.text = transformTimeToHHMM(driveItem?.startTime!!)
                tv_end_time.text = transformTimeToHHMM(driveItem?.endTime!!)

                btn_drive_history.setOnClickListener {
                    var intent = Intent(context, DetailDriveHistoryActivity::class.java)
                    intent.putExtra("tracking_id", driveItem?.id)
                    context.startActivity(intent)
                }

                return listItemView
            } else{
                var listItemView = LayoutInflater.from(context).inflate(R.layout.item_drive_history_last, parent, false)

                val tv_more = listItemView!!.findViewById<TextView>(R.id.tv_more)
                val tv_last = listItemView!!.findViewById<TextView>(R.id.tv_last)

                tv_more.setOnClickListener {
                    if(!meta.afterCursor.isNullOrBlank()){
                        callback.clickedMore(meta, histories)
                    }
                }

                if(meta.afterCursor.isNullOrBlank()){
                    tv_more.visibility = GONE
                    tv_last.visibility = VISIBLE
                }else{
                    tv_more.visibility = VISIBLE
                    tv_last.visibility = GONE
                }

                return listItemView
            }
        }

        interface DriveCallback {
            fun clickedMore(meta:Meta, histories: MutableList<DriveItem>)

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
            // UTC 시간 파싱
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

            // ZonedDateTime으로 변환
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

            // 한국 시간대로 변환 (UTC+9)
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

            // HH:mm 형식으로 변환
            val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            // 포맷된 문자열 반환
            return kstTimeStr
        }

        private fun transformTimeToDate(isoDate: String):String{
            // UTC 시간 파싱
            val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

            // ZonedDateTime으로 변환
            val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

            // 한국 시간대로 변환 (UTC+9)
            val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

            // HH:mm 형식으로 변환
            val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))

            // 포맷된 문자열 반환
            return kstTimeStr
        }

    }
}