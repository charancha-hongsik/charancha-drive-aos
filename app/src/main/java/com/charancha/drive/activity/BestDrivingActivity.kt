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

class BestDrivingActivity:BaseActivity() {
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
    lateinit var tv_best_percent1:TextView
    lateinit var tv_diff_percent:TextView
    lateinit var tv_best_percent2:TextView

    lateinit var tv_date1:TextView
    lateinit var tv_date2:TextView


    lateinit var layout_barchart_best_driving:BarChart

    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_best_driving)

        init()

        setRecentDrivingDistance()

    }

    fun init(){
        layout_extra_speed_percent = findViewById(R.id.layout_extra_speed_percent)
        layout_extra_speed_extra = findViewById(R.id.layout_extra_speed_extra)
        layout_extra_speed_background = findViewById(R.id.layout_extra_speed_background)

        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener { finish() }

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        layout_barchart_best_driving = findViewById(R.id.layout_barchart_best_driving)

        tv_driving_info1 = findViewById(R.id.tv_driving_info1)
        tv_driving_info2 = findViewById(R.id.tv_driving_info2)
        tv_best_percent1 = findViewById(R.id.tv_best_percent1)
        tv_diff_percent = findViewById(R.id.tv_diff_percent)
        tv_best_percent2 = findViewById(R.id.tv_best_percent2)

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

            setMonthDrivingDistance()
            callMonthChart()
        }

        btn_six_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false

            setSixMonthDrivingDistance()
            callSixMonthChart()
        }

        btn_year_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true

            setYearDrivingDistance()
            callYearChart()
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
        tv_driving_info1.text = "최근 1일 평균 최적 주행"
        tv_driving_info2.text = "최근 내 차의\n최적 주행 비율이에요"

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                        tv_best_percent1.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.optimalDrivingPercentage) + "%"
                        tv_best_percent2.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.optimalDrivingPercentage) + "%"
                        tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.diffAverage.optimalDrivingPercentage) + "% 증가"

                        tv_date1.text = convertDateFormat(recentDrivingDistance.recentStartTime)
                        tv_date2.text = convertDateFormat(recentDrivingDistance.recentStartTime)

                        setExtraSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.optimalDrivingPercentage.toFloat()/100)

                        apiService().getDrivingDistanceRatioGraphData(
                            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
                            PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
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
                                TODO("Not yet implemented")
                            }

                        })

                    }else{
                        tv_best_percent1.text = "0.0"
                        tv_best_percent2.text = "0.0"
                        tv_diff_percent.text = "+0.0% 증가"
                    }


                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_best_percent1.text = "0.0"
                tv_best_percent2.text = "0.0"
                tv_diff_percent.text = "+0.0% 증가"
            }

        })



    }


    private fun setMonthDrivingDistance(){
        tv_driving_info1.text = "1개월 평균 최적 주행"
        tv_driving_info2.text = "1개월 간 내 차의\n최적 주행 비율이에요"



        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                    tv_best_percent1.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_best_percent2.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.optimalDrivingPercentage) + "% 증가"

                    tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
                    tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)

                    setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.optimalDrivingPercentage.toFloat()/100)

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_driving_info1.text = "6개월 평균 최적 주행"
        tv_driving_info2.text = "6개월 간 내 차의\n최적 주행 비율이에요"

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(150).second,
            getCurrentAndPastTimeForISO(150).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )
                    tv_best_percent1.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_best_percent2.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.optimalDrivingPercentage) + "% 증가"

                    tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
                    tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)

                    setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.optimalDrivingPercentage.toFloat()/100)

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_driving_info1.text = "1년 평균 최적 주행"
        tv_driving_info2.text = "1년 간 내 차의\n최적 주행 비율이에요"


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(334).second,
            getCurrentAndPastTimeForISO(334).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )
                    tv_best_percent1.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_best_percent2.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.optimalDrivingPercentage) + "%"
                    tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.optimalDrivingPercentage) + "% 증가"

                    tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
                    tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)

                    setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.optimalDrivingPercentage.toFloat()/100)
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun callMonthChart(){
        apiService().getDrivingDistanceRatioGraphData(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
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
                TODO("Not yet implemented")
            }

        })
    }

    private fun callSixMonthChart(){
        apiService().getDrivingDistanceRatioGraphData(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
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
                TODO("Not yet implemented")
            }

        })
    }

    private fun callYearChart(){
        apiService().getDrivingDistanceRatioGraphData(
            "Bearer " + PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@BestDrivingActivity, PreferenceUtil.USER_CARID, "")!!,
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
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChartAsDefault() {

        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

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

        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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

        layout_barchart_best_driving.invalidate() // refresh
    }


    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChart(items : List<GraphItem>) {
        var max = 0

        for(item in items){
            if(item.optimalDrivingDistancePercentage > max.toDouble())
                max = item.optimalDrivingDistancePercentage.toInt()
        }

        if(max == 0){
            setRecentBarChartAsDefault()
            return
        }

        val distances = FloatArray(24) { 0f }

        // Iterate over each item and parse the startTime to extract the hour
        val koreaZoneId = ZoneId.of("Asia/Seoul")

        // Iterate over each item and parse the startTime to extract the hour
        for (item in items) {
            val startTime = Instant.parse(item.startTime)
            val localDateTime = LocalDateTime.ofInstant(startTime, koreaZoneId)
            val hour = localDateTime.hour


            distances[hour] = item.optimalDrivingDistancePercentage.toFloat()
        }


        val entries = listOf(
            BarEntry(-1f, distances.get(0)), // 00시
            BarEntry(-0f, distances.get(1)), // 01시
            BarEntry(1f, distances.get(2)), // 02시
            BarEntry(2f, distances.get(3)), // 03시
            BarEntry(3f, distances.get(4)), // 04시
            BarEntry(4f, distances.get(5)), // 05시
            BarEntry(5f, distances.get(6)), // 06시
            BarEntry(6f, distances.get(7)), // 07시
            BarEntry(7f, distances.get(8)), // 08시
            BarEntry(8f, distances.get(9)), // 09시
            BarEntry(9f, distances.get(10)), // 10시
            BarEntry(10f, distances.get(11)), // 11시
            BarEntry(11f, distances.get(12)), // 12시
            BarEntry(12f, distances.get(13)), // 13시
            BarEntry(13f, distances.get(14)), // 14시
            BarEntry(14f, distances.get(15)), // 15시
            BarEntry(15f, distances.get(16)), // 16시
            BarEntry(16f, distances.get(17)), // 17시
            BarEntry(17f, distances.get(18)), // 18시
            BarEntry(18f, distances.get(19)), // 19시
            BarEntry(19f, distances.get(20)), // 20시
            BarEntry(20f,distances.get(21)), // 21시
            BarEntry(21f,distances.get(22)), // 22시
            BarEntry(22f,distances.get(23)) // 23시
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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
                    value.toInt().toString() + "%"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_best_driving.invalidate() // refresh
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

        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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

        layout_barchart_best_driving.invalidate() // refresh
    }

    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart(items : List<GraphItem>, dates:List<String>) {

        var max = 0

        for(item in items){
            if(item.optimalDrivingDistancePercentage > max.toDouble())
                max = item.optimalDrivingDistancePercentage.toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault(dates)
            return
        }

        val entries = listOf(
            BarEntry(-1f, items.get(0).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(-0f, items.get(1).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(1f, items.get(2).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(2f, items.get(3).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(3f, items.get(4).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(4f, items.get(5).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(5f, items.get(6).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(6f, items.get(7).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(7f, items.get(8).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(8f, items.get(9).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(9f, items.get(10).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(10f, items.get(11).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(11f, items.get(12).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(12f, items.get(13).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(13f, items.get(14).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(14f, items.get(15).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(15f, items.get(16).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(16f, items.get(17).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(17f, items.get(18).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(18f, items.get(19).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(19f, items.get(20).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(20f,items.get(21).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(21f,items.get(22).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(22f,items.get(23).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(23f,items.get(24).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(24f,items.get(25).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(25f,items.get(26).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(26f,items.get(27).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(27f,items.get(28).optimalDrivingDistancePercentage.toFloat()),
            BarEntry(28f,items.get(29).optimalDrivingDistancePercentage.toFloat())
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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
                    value.toInt().toString() + "%"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_best_driving.invalidate() // refresh
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
        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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

        layout_barchart_best_driving.invalidate() // refresh
    }

    /**
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChart(items : List<GraphItem>, months:List<String>) {

        var max = 0

        for(item in items){
            if(item.optimalDrivingDistancePercentage > max.toDouble())
                max = item.optimalDrivingDistancePercentage.toInt()
        }

        if(max == 0){
            setSixMonthBarChartAsDefault(months)
            return
        }


        val entries = listOf(
            BarEntry(-1f, items.get(0).optimalDrivingDistancePercentage.toFloat()), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, items.get(1).optimalDrivingDistancePercentage.toFloat()), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, items.get(2).optimalDrivingDistancePercentage.toFloat()), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, items.get(3).optimalDrivingDistancePercentage.toFloat()), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, items.get(4).optimalDrivingDistancePercentage.toFloat()), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, items.get(5).optimalDrivingDistancePercentage.toFloat()) // 여섯번째 월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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
                    value.toInt().toString() + "%"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_best_driving.invalidate() // refresh
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
        barData.barWidth = 1.0f
        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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

        layout_barchart_best_driving.invalidate() // refresh
    }

    private fun setYearBarChart(items : List<GraphItem>, months:List<String>) {
        var max = 0

        for(item in items){
            if(item.optimalDrivingDistancePercentage > max.toDouble())
                max = item.optimalDrivingDistancePercentage.toInt()
        }

        if(max == 0){
            setYearBarChartAsDefault(months)
            return
        }



        val entries = listOf(
            BarEntry(-1f, items.get(0).optimalDrivingDistancePercentage.toFloat()), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, items.get(1).optimalDrivingDistancePercentage.toFloat()), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, items.get(2).optimalDrivingDistancePercentage.toFloat()), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, items.get(3).optimalDrivingDistancePercentage.toFloat()), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, items.get(4).optimalDrivingDistancePercentage.toFloat()), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, items.get(5).optimalDrivingDistancePercentage.toFloat()), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, items.get(6).optimalDrivingDistancePercentage.toFloat()), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, items.get(7).optimalDrivingDistancePercentage.toFloat()), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, items.get(8).optimalDrivingDistancePercentage.toFloat()), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, items.get(9).optimalDrivingDistancePercentage.toFloat()), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, items.get(10).optimalDrivingDistancePercentage.toFloat()), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,items.get(11).optimalDrivingDistancePercentage.toFloat()) // 12월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 1.0f
        layout_barchart_best_driving.data = barData
        layout_barchart_best_driving.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_best_driving.description.isEnabled = false
        layout_barchart_best_driving.animateY(1000)
        layout_barchart_best_driving.legend.isEnabled = false
        layout_barchart_best_driving.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_best_driving.xAxis
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
        val leftAxis = layout_barchart_best_driving.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_best_driving.axisRight
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

        layout_barchart_best_driving.invalidate() // refresh
    }

}