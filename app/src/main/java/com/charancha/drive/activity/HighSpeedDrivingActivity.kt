package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetDrivingGraphDataResponse
import com.charancha.drive.retrofit.response.GetDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GetRecentDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GraphItem
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class HighSpeedDrivingActivity:BaseRefreshActivity() {
    lateinit var layout_high_speed_percent: View
    lateinit var layout_high_speed_extra:View
    lateinit var layout_high_speed_background:ConstraintLayout

    lateinit var layout_low_speed_percent: View
    lateinit var layout_low_speed_extra:View
    lateinit var layout_low_speed_background:ConstraintLayout

    lateinit var layout_extra_speed_percent: View
    lateinit var layout_extra_speed_extra:View
    lateinit var layout_extra_speed_background:ConstraintLayout

    lateinit var btn_back:ImageView
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView

    lateinit var tv_driving_info1:TextView
    lateinit var tv_driving_info2:TextView
    lateinit var tv_driving_info3:TextView

    lateinit var tv_total_percent:TextView
    lateinit var tv_diff_percent:TextView
    lateinit var tv_high_speed_percent:TextView
    lateinit var tv_low_speed_percent:TextView
    lateinit var tv_etc_speed_percent:TextView

    lateinit var tv_date1:TextView
    lateinit var tv_date2:TextView


    lateinit var layout_barchart_highspeed:BarChart

    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highspeed_driving)

        init()
        setRecentDrivingDistance()

    }

    fun init(){
        layout_high_speed_background = findViewById(R.id.layout_high_speed_background)
        layout_high_speed_percent = findViewById(R.id.layout_high_speed_percent)
        layout_high_speed_extra = findViewById(R.id.layout_high_speed_extra)

        layout_low_speed_percent = findViewById(R.id.layout_low_speed_percent)
        layout_low_speed_extra = findViewById(R.id.layout_low_speed_extra)
        layout_low_speed_background = findViewById(R.id.layout_low_speed_background)

        layout_extra_speed_percent = findViewById(R.id.layout_extra_speed_percent)
        layout_extra_speed_extra = findViewById(R.id.layout_extra_speed_extra)
        layout_extra_speed_background = findViewById(R.id.layout_extra_speed_background)

        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener { finish() }

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        layout_barchart_highspeed = findViewById(R.id.layout_barchart_highspeed)

        tv_driving_info1 = findViewById(R.id.tv_driving_info1)
        tv_driving_info2 = findViewById(R.id.tv_driving_info2)
        tv_driving_info3 = findViewById(R.id.tv_driving_info3)

        tv_total_percent = findViewById(R.id.tv_total_percent)
        tv_diff_percent = findViewById(R.id.tv_diff_percent)

        tv_high_speed_percent = findViewById(R.id.tv_high_speed_percent)
        tv_low_speed_percent = findViewById(R.id.tv_low_speed_percent)
        tv_etc_speed_percent = findViewById(R.id.tv_etc_speed_percent)

        tv_date1 = findViewById(R.id.tv_date1)
        tv_date2 = findViewById(R.id.tv_date2)

        btn_recent_drive.isSelected = true

        btn_recent_drive.setOnClickListener {
            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            setRecentDrivingDistance()

        }

        btn_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            callMonthChart()
            setMonthDrivingDistance()
        }

        btn_six_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false

            callSixMonthChart()
            setSixMonthDrivingDistance()
        }

        btn_year_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true

            callYearChart()
            setYearDrivingDistance()
        }
    }


    /**
     * 0.0 ~ 1
     */
    fun setHighSpeedDrivingChartWidthByPercent(percent:Float){
        layout_high_speed_background.post {
            val backgroundWidth = layout_high_speed_background.width


            if(percent == 0.0f){
                layout_high_speed_percent.visibility = GONE

                val layoutParams2 = layout_high_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth
                layout_high_speed_extra.layoutParams = layoutParams2

            }else{
                layout_high_speed_percent.visibility = VISIBLE
                // Calculate 70% of the background view's width
                val chartWidth = (backgroundWidth * percent).toInt()

                // Apply the calculated width to view_normal_speed_driving_chart
                val layoutParams = layout_high_speed_percent.layoutParams
                layoutParams.width = chartWidth
                layout_high_speed_percent.layoutParams = layoutParams

                val layoutParams2 = layout_high_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth - chartWidth
                layout_high_speed_extra.layoutParams = layoutParams2
            }
        }
    }

    /**
     * 0.0 ~ 1
     */
    fun setLowSpeedDrivingChartWidthByPercent(percent:Float){
        layout_low_speed_background.post {
            val backgroundWidth = layout_low_speed_background.width

            if(percent == 0f){
                layout_low_speed_percent.visibility = GONE

                val layoutParams2 = layout_low_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth
                layout_low_speed_extra.layoutParams = layoutParams2

            }else{
                layout_low_speed_percent.visibility = VISIBLE

                // Calculate 70% of the background view's width
                val chartWidth = (backgroundWidth * percent).toInt()

                // Apply the calculated width to view_normal_speed_driving_chart
                val layoutParams = layout_low_speed_percent.layoutParams
                layoutParams.width = chartWidth
                layout_low_speed_percent.layoutParams = layoutParams

                val layoutParams2 = layout_low_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth - chartWidth
                layout_low_speed_extra.layoutParams = layoutParams2
            }
        }
    }

    /**
     * 0.0 ~ 1
     */
    fun setExtraSpeedDrivingChartWidthByPercent(percent:Float){
        layout_extra_speed_background.post {
            val backgroundWidth = layout_extra_speed_background.width


            if(percent == 0f){
                layout_extra_speed_percent.visibility = GONE

                val layoutParams2 = layout_extra_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth
                layout_extra_speed_extra.layoutParams = layoutParams2

            }else{
                layout_extra_speed_percent.visibility = VISIBLE

                // Calculate 70% of the background view's width
                val chartWidth = (backgroundWidth * percent).toInt()

                // Apply the calculated width to view_normal_speed_driving_chart
                val layoutParams = layout_extra_speed_percent.layoutParams
                layoutParams.width = chartWidth
                layout_extra_speed_percent.layoutParams = layoutParams

                val layoutParams2 = layout_extra_speed_extra.layoutParams
                layoutParams2.width = backgroundWidth - chartWidth
                layout_extra_speed_extra.layoutParams = layoutParams2
            }
        }
    }

    private fun setRecentDrivingDistance(){

        tv_date1.text = convertDateFormat(recentStartTime)
        tv_date2.text = convertDateFormat(recentStartTime)

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val recentDrivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetRecentDrivingStatisticsResponse::class.java
                    )
                    if(recentDrivingDistance.isRecent){
                        recentStartTime = recentDrivingDistance.recentStartTime
                        recentEndTime = recentDrivingDistance.recentEndTime

                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.highSpeedDrivingDistancePercentage)

                        if(recentDrivingDistance.diffAverage.highSpeedDrivingDistancePercentage == 0.0){
                            tv_diff_percent.text = "거리 변동이 없어요."
                            tv_diff_percent.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(recentDrivingDistance.diffTotal.highSpeedDrivingDistancePercentage > 0.0){
                            tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(recentDrivingDistance.diffTotal.highSpeedDrivingDistancePercentage < 0.0){
                            tv_diff_percent.text = "-" + String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 감소"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.highSpeedDrivingDistancePercentage) + "%"
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.lowSpeedDrivingDistancePercentage) + "%"
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.etcSpeedDrivingDistancePercentage) + "%"

                        tv_driving_info1.text = "최근 1일 평균 고속 주행"
                        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
                        tv_driving_info3.text = "최근 내 차의\n고속 주행 비율율이에요"

                        setHighSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)

                        tv_date1.text = convertDateFormat(recentStartTime)
                        tv_date2.text = convertDateFormat(recentStartTime)

                        apiService().getDrivingDistanceGraphData(
                            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
                            PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
                            "ASC",
                            null,
                            null,
                            recentStartTime,
                            recentEndTime,
                            "startTime",
                            "hour"
                        ).enqueue(object :Callback<ResponseBody>{
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {

                                if(response.code() == 200){
                                    val getDrivingGraphDataResponse = Gson().fromJson(
                                        response.body()?.string(),
                                        GetDrivingGraphDataResponse::class.java
                                    )
                                    setRecentBarChart(getDrivingGraphDataResponse.items)
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            }

                        })

                    }else{
                        tv_total_percent.text = "0.0"
                        tv_diff_percent.text = "+0.0% 증가"
                        tv_high_speed_percent.text = 0.0.toString()
                        tv_low_speed_percent.text = 0.0.toString()
                        tv_etc_speed_percent.text = 0.0.toString()

                        setRecentBarChartAsDefault()

                        setHighSpeedDrivingChartWidthByPercent(0f)
                        setLowSpeedDrivingChartWidthByPercent(0f)
                        setExtraSpeedDrivingChartWidthByPercent(0f)


                    }
                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_percent.text = "0.0"
                tv_diff_percent.text = "+0.0% 증가"
                tv_high_speed_percent.text = 0.0.toString()
                tv_low_speed_percent.text = 0.0.toString()
                tv_etc_speed_percent.text = 0.0.toString()

                setRecentBarChartAsDefault()

                setHighSpeedDrivingChartWidthByPercent(0f)
                setLowSpeedDrivingChartWidthByPercent(0f)
                setExtraSpeedDrivingChartWidthByPercent(0f)
            }

        })



    }

    private fun setMonthDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )


                    if(drivingDistance.total.totalDistance != 0.0){
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)

                        if(drivingDistance.diffAverage.highSpeedDrivingDistancePercentage == 0.0){
                            tv_diff_percent.text = "거리 변동이 없어요."
                            tv_diff_percent.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage > 0.0){
                            tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage < 0.0){
                            tv_diff_percent.text = "-" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 감소"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage) + "%"
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage) + "%"
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage) + "%"

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)

                        tv_driving_info1.text = "1개월 평균 고속 주행"
                        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
                        tv_driving_info3.text = "1개월 내 차의\n고속 주행 비율이에요"
                    }else{
                        tv_total_percent.text = "0.0"
                        tv_diff_percent.text = "+0.0% 증가"
                        tv_high_speed_percent.text = 0.0.toString()
                        tv_low_speed_percent.text = 0.0.toString()
                        tv_etc_speed_percent.text = 0.0.toString()

                        setRecentBarChartAsDefault()

                        setHighSpeedDrivingChartWidthByPercent(0f)
                        setLowSpeedDrivingChartWidthByPercent(0f)
                        setExtraSpeedDrivingChartWidthByPercent(0f)
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_percent.text = "0.0"
                tv_diff_percent.text = "+0.0% 증가"
                tv_high_speed_percent.text = 0.0.toString()
                tv_low_speed_percent.text = 0.0.toString()
                tv_etc_speed_percent.text = 0.0.toString()

                setRecentBarChartAsDefault()

                setHighSpeedDrivingChartWidthByPercent(0f)
                setLowSpeedDrivingChartWidthByPercent(0f)
                setExtraSpeedDrivingChartWidthByPercent(0f)
            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(150).second,
            getCurrentAndPastTimeForISO(150).first,
            "startTime",
            "month").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(drivingDistance.total.totalDistance != 0.0){
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)

                        if(drivingDistance.diffAverage.highSpeedDrivingDistancePercentage == 0.0){
                            tv_diff_percent.text = "거리 변동이 없어요."
                            tv_diff_percent.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage > 0.0){
                            tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage < 0.0){
                            tv_diff_percent.text = "-" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 감소"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage) + "%"
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage) + "%"
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage) + "%"

                        tv_driving_info1.text = "6개월 평균 고속 주행"
                        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
                        tv_driving_info3.text = "6개월 내 차의\n고속 주행 비율이에요"

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)
                    }else{
                        tv_total_percent.text = "0.0"
                        tv_diff_percent.text = "+0.0% 증가"
                        tv_high_speed_percent.text = 0.0.toString()
                        tv_low_speed_percent.text = 0.0.toString()
                        tv_etc_speed_percent.text = 0.0.toString()


                        tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                        setRecentBarChartAsDefault()

                        setHighSpeedDrivingChartWidthByPercent(0f)
                        setLowSpeedDrivingChartWidthByPercent(0f)
                        setExtraSpeedDrivingChartWidthByPercent(0f)
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_percent.text = "0.0"
                tv_diff_percent.text = "+0.0% 증가"
                tv_high_speed_percent.text = 0.0.toString()
                tv_low_speed_percent.text = 0.0.toString()
                tv_etc_speed_percent.text = 0.0.toString()


                tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                setRecentBarChartAsDefault()

                setHighSpeedDrivingChartWidthByPercent(0f)
                setLowSpeedDrivingChartWidthByPercent(0f)
                setExtraSpeedDrivingChartWidthByPercent(0f)
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(334).second,
            getCurrentAndPastTimeForISO(334).first,
            "startTime",
            "month").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(drivingDistance.total.totalDistance != 0.0){
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)

                        if(drivingDistance.diffAverage.highSpeedDrivingDistancePercentage == 0.0){
                            tv_diff_percent.text = "거리 변동이 없어요."
                            tv_diff_percent.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage > 0.0){
                            tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffTotal.highSpeedDrivingDistancePercentage < 0.0){
                            tv_diff_percent.text = "-" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 감소"
                            tv_diff_percent.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage) + "%"
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage) + "%"
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage) + "%"

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)

                        tv_driving_info1.text = "1년 평균 고속 주행"
                        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
                        tv_driving_info3.text = "1년 내 차의\n고속 주행 비율이에요"
                    }else{
                        tv_total_percent.text = "0.0"
                        tv_diff_percent.text = "+0.0% 증가"
                        tv_high_speed_percent.text = 0.0.toString()
                        tv_low_speed_percent.text = 0.0.toString()
                        tv_etc_speed_percent.text = 0.0.toString()

                        setRecentBarChartAsDefault()

                        setHighSpeedDrivingChartWidthByPercent(0f)
                        setLowSpeedDrivingChartWidthByPercent(0f)
                        setExtraSpeedDrivingChartWidthByPercent(0f)
                    }



                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_percent.text = "0.0"
                tv_diff_percent.text = "+0.0% 증가"
                tv_high_speed_percent.text = 0.0.toString()
                tv_low_speed_percent.text = 0.0.toString()
                tv_etc_speed_percent.text = 0.0.toString()

                setRecentBarChartAsDefault()

                setHighSpeedDrivingChartWidthByPercent(0f)
                setLowSpeedDrivingChartWidthByPercent(0f)
                setExtraSpeedDrivingChartWidthByPercent(0f)
            }

        })
    }

    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChartAsDefault() {

        val entries = listOf(
            BarEntry(-1f, 0f),
            BarEntry(-0f, 0f),
            BarEntry(1f, 0f),
            BarEntry(2f, 0f),
            BarEntry(3f, 0f),
            BarEntry(4f, 0f),
            BarEntry(5f, 0f),
            BarEntry(6f, 0f),
            BarEntry(7f, 0f),
            BarEntry(8f, 0f),
            BarEntry(9f, 0f),
            BarEntry(10f, 0f),
            BarEntry(11f, 0f),
            BarEntry(12f, 0f),
            BarEntry(13f, 0f),
            BarEntry(14f, 0f),
            BarEntry(15f, 0f),
            BarEntry(16f, 0f),
            BarEntry(17f, 0f),
            BarEntry(18f, 0f),
            BarEntry(19f, 0f),
            BarEntry(20f,0f),
            BarEntry(21f,0f),
            BarEntry(22f,0f)
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 23f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 24

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    1 -> "오전 12시"
                    8 -> "오전 6시"
                    15 -> "오후 12시"
                    21-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }


    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChart(items : List<GraphItem>) {
        var max = 0

        for(item in items){
            val sum = transferDistance(item.highSpeedDrivingDistance).toDouble() + transferDistance(item.lowSpeedDrivingDistance).toDouble() + transferDistance(item.etcSpeedDrivingDistance).toDouble()
            if(sum > max.toDouble())
                max = sum.toInt()
        }

        if(max == 0){
            setRecentBarChartAsDefault()
            return
        }

        val highSpeedDrivingDistances = FloatArray(24) { 0f }
        val lowSpeedDrivingDistances = FloatArray(24) { 0f }
        val etcSpeedDrivingDistances = FloatArray(24) { 0f }


        // Iterate over each item and parse the startTime to extract the hour
        val koreaZoneId = ZoneId.of("Asia/Seoul")

        // Iterate over each item and parse the startTime to extract the hour
        for (item in items) {
            val startTime = Instant.parse(item.startTime)
            val localDateTime = LocalDateTime.ofInstant(startTime, koreaZoneId)
            val hour = localDateTime.hour

            highSpeedDrivingDistances[hour] = transferDistance(item.highSpeedDrivingDistance).toFloat()
        }

        for (item in items) {
            val startTime = Instant.parse(item.startTime)
            val localDateTime = LocalDateTime.ofInstant(startTime, koreaZoneId)
            val hour = localDateTime.hour

            lowSpeedDrivingDistances[hour] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat()
        }

        for (item in items) {
            val startTime = Instant.parse(item.startTime)
            val localDateTime = LocalDateTime.ofInstant(startTime, koreaZoneId)
            val hour = localDateTime.hour

            etcSpeedDrivingDistances[hour] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat() + transferDistance(item.etcSpeedDrivingDistance).toFloat()
        }

        val entries1 = listOf(
            BarEntry(-1f, etcSpeedDrivingDistances.get(0)), // 00시
            BarEntry(-0f, etcSpeedDrivingDistances.get(1)), // 01시
            BarEntry(1f, etcSpeedDrivingDistances.get(2)), // 02시
            BarEntry(2f, etcSpeedDrivingDistances.get(3)), // 03시
            BarEntry(3f, etcSpeedDrivingDistances.get(4)), // 04시
            BarEntry(4f, etcSpeedDrivingDistances.get(5)), // 05시
            BarEntry(5f, etcSpeedDrivingDistances.get(6)), // 06시
            BarEntry(6f, etcSpeedDrivingDistances.get(7)), // 07시
            BarEntry(7f, etcSpeedDrivingDistances.get(8)), // 08시
            BarEntry(8f, etcSpeedDrivingDistances.get(9)), // 09시
            BarEntry(9f, etcSpeedDrivingDistances.get(10)), // 10시
            BarEntry(10f, etcSpeedDrivingDistances.get(11)), // 11시
            BarEntry(11f, etcSpeedDrivingDistances.get(12)), // 12시
            BarEntry(12f, etcSpeedDrivingDistances.get(13)), // 13시
            BarEntry(13f, etcSpeedDrivingDistances.get(14)), // 14시
            BarEntry(14f, etcSpeedDrivingDistances.get(15)), // 15시
            BarEntry(15f, etcSpeedDrivingDistances.get(16)), // 16시
            BarEntry(16f, etcSpeedDrivingDistances.get(17)), // 17시
            BarEntry(17f, etcSpeedDrivingDistances.get(18)), // 18시
            BarEntry(18f, etcSpeedDrivingDistances.get(19)), // 19시
            BarEntry(19f, etcSpeedDrivingDistances.get(20)), // 20시
            BarEntry(20f,etcSpeedDrivingDistances.get(21)), // 21시
            BarEntry(21f,etcSpeedDrivingDistances.get(22)), // 22시
            BarEntry(22f,etcSpeedDrivingDistances.get(23)) // 23시
        )

        val entries2 = listOf(
            BarEntry(-1f, lowSpeedDrivingDistances.get(0)), // 00시
            BarEntry(-0f, lowSpeedDrivingDistances.get(1)), // 01시
            BarEntry(1f, lowSpeedDrivingDistances.get(2)), // 02시
            BarEntry(2f, lowSpeedDrivingDistances.get(3)), // 03시
            BarEntry(3f, lowSpeedDrivingDistances.get(4)), // 04시
            BarEntry(4f, lowSpeedDrivingDistances.get(5)), // 05시
            BarEntry(5f, lowSpeedDrivingDistances.get(6)), // 06시
            BarEntry(6f, lowSpeedDrivingDistances.get(7)), // 07시
            BarEntry(7f, lowSpeedDrivingDistances.get(8)), // 08시
            BarEntry(8f, lowSpeedDrivingDistances.get(9)), // 09시
            BarEntry(9f, lowSpeedDrivingDistances.get(10)), // 10시
            BarEntry(10f, lowSpeedDrivingDistances.get(11)), // 11시
            BarEntry(11f, lowSpeedDrivingDistances.get(12)), // 12시
            BarEntry(12f, lowSpeedDrivingDistances.get(13)), // 13시
            BarEntry(13f, lowSpeedDrivingDistances.get(14)), // 14시
            BarEntry(14f, lowSpeedDrivingDistances.get(15)), // 15시
            BarEntry(15f, lowSpeedDrivingDistances.get(16)), // 16시
            BarEntry(16f, lowSpeedDrivingDistances.get(17)), // 17시
            BarEntry(17f, lowSpeedDrivingDistances.get(18)), // 18시
            BarEntry(18f, lowSpeedDrivingDistances.get(19)), // 19시
            BarEntry(19f, lowSpeedDrivingDistances.get(20)), // 20시
            BarEntry(20f,lowSpeedDrivingDistances.get(21)), // 21시
            BarEntry(21f,lowSpeedDrivingDistances.get(22)), // 22시
            BarEntry(22f,lowSpeedDrivingDistances.get(23)) // 23시
        )

        val entries3 = listOf(
            BarEntry(-1f, highSpeedDrivingDistances.get(0)), // 00시
            BarEntry(-0f, highSpeedDrivingDistances.get(1)), // 01시
            BarEntry(1f, highSpeedDrivingDistances.get(2)), // 02시
            BarEntry(2f, highSpeedDrivingDistances.get(3)), // 03시
            BarEntry(3f, highSpeedDrivingDistances.get(4)), // 04시
            BarEntry(4f, highSpeedDrivingDistances.get(5)), // 05시
            BarEntry(5f, highSpeedDrivingDistances.get(6)), // 06시
            BarEntry(6f, highSpeedDrivingDistances.get(7)), // 07시
            BarEntry(7f, highSpeedDrivingDistances.get(8)), // 08시
            BarEntry(8f, highSpeedDrivingDistances.get(9)), // 09시
            BarEntry(9f, highSpeedDrivingDistances.get(10)), // 10시
            BarEntry(10f, highSpeedDrivingDistances.get(11)), // 11시
            BarEntry(11f, highSpeedDrivingDistances.get(12)), // 12시
            BarEntry(12f, highSpeedDrivingDistances.get(13)), // 13시
            BarEntry(13f, highSpeedDrivingDistances.get(14)), // 14시
            BarEntry(14f, highSpeedDrivingDistances.get(15)), // 15시
            BarEntry(15f, highSpeedDrivingDistances.get(16)), // 16시
            BarEntry(16f, highSpeedDrivingDistances.get(17)), // 17시
            BarEntry(17f, highSpeedDrivingDistances.get(18)), // 18시
            BarEntry(18f, highSpeedDrivingDistances.get(19)), // 19시
            BarEntry(19f, highSpeedDrivingDistances.get(20)), // 20시
            BarEntry(20f,highSpeedDrivingDistances.get(21)), // 21시
            BarEntry(21f,highSpeedDrivingDistances.get(22)), // 22시
            BarEntry(22f,highSpeedDrivingDistances.get(23)) // 23시
        )

        val dataSet1 = BarDataSet(entries1, "Part 1")
        dataSet1.color = getColor(R.color.gray_700)
        dataSet1.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val dataSet2 = BarDataSet(entries2, "Part 2")
        dataSet2.color = getColor(R.color.gray_950)
        dataSet2.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정


        val dataSet3 = BarDataSet(entries3, "Part 3")
        dataSet3.color = getColor(R.color.sec_500)
        dataSet3.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정



        val barData = BarData(dataSet1, dataSet2, dataSet3)
        barData.barWidth = 0.6f

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 23f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 24

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    0 -> "오전 12시"
                    6 -> "오전 6시"
                    12 -> "오후 12시"
                    18-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }


    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChartAsDefault(months: List<String>) {
        val entries = listOf(
            BarEntry(-1f, 0f),
            BarEntry(-0f, 0f),
            BarEntry(1f, 0f),
            BarEntry(2f, 0f),
            BarEntry(3f, 0f),
            BarEntry(4f, 0f),
            BarEntry(5f, 0f),
            BarEntry(6f, 0f),
            BarEntry(7f, 0f),
            BarEntry(8f, 0f),
            BarEntry(9f, 0f),
            BarEntry(10f, 0f),
            BarEntry(11f, 0f),
            BarEntry(12f, 0f),
            BarEntry(13f, 0f),
            BarEntry(14f, 0f),
            BarEntry(15f, 0f),
            BarEntry(16f, 0f),
            BarEntry(17f, 0f),
            BarEntry(18f, 0f),
            BarEntry(19f, 0f),
            BarEntry(20f,0f),
            BarEntry(21f,0f),
            BarEntry(22f,0f),
            BarEntry(23f,0f),
            BarEntry(24f,0f),
            BarEntry(25f,0f),
            BarEntry(26f,0f),
            BarEntry(27f,0f),
            BarEntry(28f,0f)
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 29f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 30

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    1 -> months.get(0)
                    10 -> months.get(1)
                    18 -> months.get(2)
                    26-> months.get(3)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }

    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart(items : List<GraphItem>, dates:List<String>) {

        var max = 0

        for(item in items){
            val sum = transferDistance(item.highSpeedDrivingDistance).toDouble() + transferDistance(item.lowSpeedDrivingDistance).toDouble() + transferDistance(item.etcSpeedDrivingDistance).toDouble()
            if(sum > max.toDouble())
                max = sum.toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault(dates)
            return
        }

        val highSpeedDrivingDistances = FloatArray(30) { 0f }
        val lowSpeedDrivingDistances = FloatArray(30) { 0f }
        val etcSpeedDrivingDistances = FloatArray(30) { 0f }

        for((index,item) in items.withIndex()){
            highSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat()
            lowSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat()
            etcSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat() + transferDistance(item.etcSpeedDrivingDistance).toFloat()
        }
        val entries1 = listOf(
            BarEntry(-1f, etcSpeedDrivingDistances.get(0)),
            BarEntry(-0f, etcSpeedDrivingDistances.get(1)),
            BarEntry(1f, etcSpeedDrivingDistances.get(2)),
            BarEntry(2f, etcSpeedDrivingDistances.get(3)),
            BarEntry(3f, etcSpeedDrivingDistances.get(4)),
            BarEntry(4f, etcSpeedDrivingDistances.get(5)),
            BarEntry(5f, etcSpeedDrivingDistances.get(6)),
            BarEntry(6f, etcSpeedDrivingDistances.get(7)),
            BarEntry(7f, etcSpeedDrivingDistances.get(8)),
            BarEntry(8f, etcSpeedDrivingDistances.get(9)),
            BarEntry(9f, etcSpeedDrivingDistances.get(10)),
            BarEntry(10f, etcSpeedDrivingDistances.get(11)),
            BarEntry(11f, etcSpeedDrivingDistances.get(12)),
            BarEntry(12f, etcSpeedDrivingDistances.get(13)),
            BarEntry(13f, etcSpeedDrivingDistances.get(14)),
            BarEntry(14f, etcSpeedDrivingDistances.get(15)),
            BarEntry(15f, etcSpeedDrivingDistances.get(16)),
            BarEntry(16f, etcSpeedDrivingDistances.get(17)),
            BarEntry(17f, etcSpeedDrivingDistances.get(18)),
            BarEntry(18f, etcSpeedDrivingDistances.get(19)),
            BarEntry(19f, etcSpeedDrivingDistances.get(20)),
            BarEntry(20f,etcSpeedDrivingDistances.get(21)),
            BarEntry(21f,etcSpeedDrivingDistances.get(22)),
            BarEntry(22f,etcSpeedDrivingDistances.get(23)),
            BarEntry(23f,etcSpeedDrivingDistances.get(24)),
            BarEntry(24f,etcSpeedDrivingDistances.get(25)),
            BarEntry(25f,etcSpeedDrivingDistances.get(26)),
            BarEntry(26f,etcSpeedDrivingDistances.get(27)),
            BarEntry(27f,etcSpeedDrivingDistances.get(28)),
            BarEntry(28f,etcSpeedDrivingDistances.get(29))
        )


        val entries2 = listOf(
            BarEntry(-1f, lowSpeedDrivingDistances.get(0)),
            BarEntry(-0f, lowSpeedDrivingDistances.get(1)),
            BarEntry(1f, lowSpeedDrivingDistances.get(2)),
            BarEntry(2f, lowSpeedDrivingDistances.get(3)),
            BarEntry(3f, lowSpeedDrivingDistances.get(4)),
            BarEntry(4f, lowSpeedDrivingDistances.get(5)),
            BarEntry(5f, lowSpeedDrivingDistances.get(6)),
            BarEntry(6f, lowSpeedDrivingDistances.get(7)),
            BarEntry(7f, lowSpeedDrivingDistances.get(8)),
            BarEntry(8f, lowSpeedDrivingDistances.get(9)),
            BarEntry(9f, lowSpeedDrivingDistances.get(10)),
            BarEntry(10f, lowSpeedDrivingDistances.get(11)),
            BarEntry(11f, lowSpeedDrivingDistances.get(12)),
            BarEntry(12f, lowSpeedDrivingDistances.get(13)),
            BarEntry(13f, lowSpeedDrivingDistances.get(14)),
            BarEntry(14f, lowSpeedDrivingDistances.get(15)),
            BarEntry(15f, lowSpeedDrivingDistances.get(16)),
            BarEntry(16f, lowSpeedDrivingDistances.get(17)),
            BarEntry(17f, lowSpeedDrivingDistances.get(18)),
            BarEntry(18f, lowSpeedDrivingDistances.get(19)),
            BarEntry(19f, lowSpeedDrivingDistances.get(20)),
            BarEntry(20f,lowSpeedDrivingDistances.get(21)),
            BarEntry(21f,lowSpeedDrivingDistances.get(22)),
            BarEntry(22f,lowSpeedDrivingDistances.get(23)),
            BarEntry(23f,lowSpeedDrivingDistances.get(24)),
            BarEntry(24f,lowSpeedDrivingDistances.get(25)),
            BarEntry(25f,lowSpeedDrivingDistances.get(26)),
            BarEntry(26f,lowSpeedDrivingDistances.get(27)),
            BarEntry(27f,lowSpeedDrivingDistances.get(28)),
            BarEntry(28f,lowSpeedDrivingDistances.get(29))
        )

        val entries3 = listOf(
            BarEntry(-1f, highSpeedDrivingDistances.get(0)),
            BarEntry(-0f, highSpeedDrivingDistances.get(1)),
            BarEntry(1f, highSpeedDrivingDistances.get(2)),
            BarEntry(2f, highSpeedDrivingDistances.get(3)),
            BarEntry(3f, highSpeedDrivingDistances.get(4)),
            BarEntry(4f, highSpeedDrivingDistances.get(5)),
            BarEntry(5f, highSpeedDrivingDistances.get(6)),
            BarEntry(6f, highSpeedDrivingDistances.get(7)),
            BarEntry(7f, highSpeedDrivingDistances.get(8)),
            BarEntry(8f, highSpeedDrivingDistances.get(9)),
            BarEntry(9f, highSpeedDrivingDistances.get(10)),
            BarEntry(10f, highSpeedDrivingDistances.get(11)),
            BarEntry(11f, highSpeedDrivingDistances.get(12)),
            BarEntry(12f, highSpeedDrivingDistances.get(13)),
            BarEntry(13f, highSpeedDrivingDistances.get(14)),
            BarEntry(14f, highSpeedDrivingDistances.get(15)),
            BarEntry(15f, highSpeedDrivingDistances.get(16)),
            BarEntry(16f, highSpeedDrivingDistances.get(17)),
            BarEntry(17f, highSpeedDrivingDistances.get(18)),
            BarEntry(18f, highSpeedDrivingDistances.get(19)),
            BarEntry(19f, highSpeedDrivingDistances.get(20)),
            BarEntry(20f,highSpeedDrivingDistances.get(21)),
            BarEntry(21f,highSpeedDrivingDistances.get(22)),
            BarEntry(22f,highSpeedDrivingDistances.get(23)),
            BarEntry(23f,highSpeedDrivingDistances.get(24)),
            BarEntry(24f,highSpeedDrivingDistances.get(25)),
            BarEntry(25f,highSpeedDrivingDistances.get(26)),
            BarEntry(26f,highSpeedDrivingDistances.get(27)),
            BarEntry(27f,highSpeedDrivingDistances.get(28)),
            BarEntry(28f,highSpeedDrivingDistances.get(29))
        )


        val dataSet1 = BarDataSet(entries1, "Part 1")
        dataSet1.color = getColor(R.color.gray_700)
        dataSet1.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val dataSet2 = BarDataSet(entries2, "Part 2")
        dataSet2.color = getColor(R.color.gray_950)
        dataSet2.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정


        val dataSet3 = BarDataSet(entries3, "Part 3")
        dataSet3.color = getColor(R.color.sec_500)
        dataSet3.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정



        val barData = BarData(dataSet1, dataSet2, dataSet3)
        barData.barWidth = 0.6f

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 29f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 30

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    1 -> dates.get(0)
                    10 -> dates.get(1)
                    18 -> dates.get(2)
                    26-> dates.get(3)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }

    private fun callMonthChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day"
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setMonthBarChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(29).third)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    private fun callSixMonthChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(150).second,
            getCurrentAndPastTimeForISO(150).first,
            "startTime",
            "month"
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setSixMonthBarChart(getDrivingGraphDataResponse.items,getCurrentAndPastTimeForISO(150).third )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    private fun callYearChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(334).second,
            getCurrentAndPastTimeForISO(334).first,
            "startTime",
            "month"
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setYearBarChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(334).third)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    /**
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChartAsDefault(months: List<String>) {

        val entries = listOf(
            BarEntry(-1f, 0f), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, 0f), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, 0f), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, 0f), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, 0f), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, 0f) // 여섯번째 월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 10f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 11

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    -1 -> months.get(0)
                    1 -> months.get(1)
                    3 -> months.get(2)
                    5 -> months.get(3)
                    7 -> months.get(4)
                    9 -> months.get(5)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }

    /**
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChart(items : List<GraphItem>, months:List<String>) {

        var max = 0

        for(item in items){
            val sum = transferDistance(item.highSpeedDrivingDistance).toDouble() + transferDistance(item.lowSpeedDrivingDistance).toDouble() + transferDistance(item.etcSpeedDrivingDistance).toDouble()
            if(sum > max.toDouble())
                max = sum.toInt()
        }

        if(max == 0){
            setSixMonthBarChartAsDefault(months)
            return
        }

        val highSpeedDrivingDistances = FloatArray(6) { 0f }
        val lowSpeedDrivingDistances = FloatArray(6) { 0f }
        val etcSpeedDrivingDistances = FloatArray(6) { 0f }

        for((index,item) in items.withIndex()){
            highSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat()
            lowSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat()
            etcSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat() + transferDistance(item.etcSpeedDrivingDistance).toFloat()
        }



        val entries1 = listOf(
            BarEntry(-1f, etcSpeedDrivingDistances.get(0)), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, etcSpeedDrivingDistances.get(1)), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, etcSpeedDrivingDistances.get(2)), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, etcSpeedDrivingDistances.get(3)), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, etcSpeedDrivingDistances.get(4)), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, etcSpeedDrivingDistances.get(5)) // 여섯번째 월
        )

        val entries2 = listOf(
            BarEntry(-1f, lowSpeedDrivingDistances.get(0)), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, lowSpeedDrivingDistances.get(1)), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, lowSpeedDrivingDistances.get(2)), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, lowSpeedDrivingDistances.get(3)), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, lowSpeedDrivingDistances.get(4)), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, lowSpeedDrivingDistances.get(5)) // 여섯번째 월
        )

        val entries3 = listOf(
            BarEntry(-1f, highSpeedDrivingDistances.get(0)), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, highSpeedDrivingDistances.get(1)), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, highSpeedDrivingDistances.get(2)), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, highSpeedDrivingDistances.get(3)), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, highSpeedDrivingDistances.get(4)), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, highSpeedDrivingDistances.get(5)) // 여섯번째 월
        )

        val dataSet1 = BarDataSet(entries1, "Part 1")
        dataSet1.color = getColor(R.color.gray_700)
        dataSet1.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val dataSet2 = BarDataSet(entries2, "Part 2")
        dataSet2.color = getColor(R.color.gray_950)
        dataSet2.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정


        val dataSet3 = BarDataSet(entries3, "Part 3")
        dataSet3.color = getColor(R.color.sec_500)
        dataSet3.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정



        val barData = BarData(dataSet1, dataSet2, dataSet3)

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 10f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 11

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    -1 -> months.get(0)
                    1 -> months.get(1)
                    3 -> months.get(2)
                    5 -> months.get(3)
                    7 -> months.get(4)
                    9 -> months.get(5)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }

    /**
     * 데어터가 12개 내려옴 (월 단위의 데이터)
     * 1월 / 5월 / 8월 / 12월
     */

    private fun setYearBarChartAsDefault(months: List<String>) {

        val entries = listOf(
            BarEntry(-1f, 0f), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, 0f), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, 0f), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, 0f), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, 0f), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, 0f), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, 0f), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, 0f), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, 0f), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, 0f), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, 0f), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,0f) // 12월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 22f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 23

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    -1 -> months.get(0)
                    7 -> months.get(4)
                    13 -> months.get(7)
                    21-> months.get(11)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }

    private fun setYearBarChart(items : List<GraphItem>, months:List<String>) {
        var max = 0

        for(item in items){
            val sum = transferDistance(item.highSpeedDrivingDistance).toDouble() + transferDistance(item.lowSpeedDrivingDistance).toDouble() + transferDistance(item.etcSpeedDrivingDistance).toDouble()
            if(sum > max.toDouble())
                max = sum.toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault(months)
            return
        }


        val highSpeedDrivingDistances = FloatArray(12) { 0f }
        val lowSpeedDrivingDistances = FloatArray(12) { 0f }
        val etcSpeedDrivingDistances = FloatArray(12) { 0f }

        for((index,item) in items.withIndex()){
            highSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat()
            lowSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat()
            etcSpeedDrivingDistances[index] = transferDistance(item.highSpeedDrivingDistance).toFloat() + transferDistance(item.lowSpeedDrivingDistance).toFloat() + transferDistance(item.etcSpeedDrivingDistance).toFloat()
        }




        val entries1 = listOf(
            BarEntry(-1f, etcSpeedDrivingDistances.get(0)), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, etcSpeedDrivingDistances.get(1)), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, etcSpeedDrivingDistances.get(2)), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, etcSpeedDrivingDistances.get(3)), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, etcSpeedDrivingDistances.get(4)), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, etcSpeedDrivingDistances.get(5)), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, etcSpeedDrivingDistances.get(6)), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, etcSpeedDrivingDistances.get(7)), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, etcSpeedDrivingDistances.get(8)), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, etcSpeedDrivingDistances.get(9)), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, etcSpeedDrivingDistances.get(10)), // 11월
            BarEntry(20f,0f),
            BarEntry(21f, etcSpeedDrivingDistances.get(11)) // 12월
        )

        val entries2 = listOf(
            BarEntry(-1f, lowSpeedDrivingDistances.get(0)), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, lowSpeedDrivingDistances.get(1)), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, lowSpeedDrivingDistances.get(2)), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, lowSpeedDrivingDistances.get(3)), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, lowSpeedDrivingDistances.get(4)), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, lowSpeedDrivingDistances.get(5)), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, lowSpeedDrivingDistances.get(6)), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, lowSpeedDrivingDistances.get(7)), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, lowSpeedDrivingDistances.get(8)), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, lowSpeedDrivingDistances.get(9)), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, lowSpeedDrivingDistances.get(10)), // 11월
            BarEntry(20f,0f),
            BarEntry(21f, lowSpeedDrivingDistances.get(11)) // 12월
        )


        val entries3 = listOf(
            BarEntry(-1f, highSpeedDrivingDistances.get(0)), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, highSpeedDrivingDistances.get(1)), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, highSpeedDrivingDistances.get(2)), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, highSpeedDrivingDistances.get(3)), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, highSpeedDrivingDistances.get(4)), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, highSpeedDrivingDistances.get(5)), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, highSpeedDrivingDistances.get(6)), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, highSpeedDrivingDistances.get(7)), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, highSpeedDrivingDistances.get(8)), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, highSpeedDrivingDistances.get(9)), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, highSpeedDrivingDistances.get(10)), // 11월
            BarEntry(20f,0f),
            BarEntry(21f, highSpeedDrivingDistances.get(11)) // 12월
        )


        val dataSet1 = BarDataSet(entries1, "Part 1")
        dataSet1.color = getColor(R.color.gray_700)
        dataSet1.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val dataSet2 = BarDataSet(entries2, "Part 2")
        dataSet2.color = getColor(R.color.gray_950)
        dataSet2.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정


        val dataSet3 = BarDataSet(entries3, "Part 3")
        dataSet3.color = getColor(R.color.sec_500)
        dataSet3.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정



        val barData = BarData(dataSet1, dataSet2, dataSet3)
        barData.barWidth = 1.0f

        layout_barchart_highspeed.data = barData
        layout_barchart_highspeed.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_highspeed.description.isEnabled = false
        layout_barchart_highspeed.animateY(1000)
        layout_barchart_highspeed.legend.isEnabled = false
        layout_barchart_highspeed.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_highspeed.xAxis
        xAxis.granularity = 1f // only intervals of 1 unit
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 22f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 23

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    -1 -> months.get(0)
                    7 -> months.get(4)
                    13 -> months.get(7)
                    21-> months.get(11)
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_barchart_highspeed.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_highspeed.invalidate() // refresh
    }



}