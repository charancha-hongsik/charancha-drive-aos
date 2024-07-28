package com.milelog.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.milelog.CommonUtil
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetDrivingGraphDataResponse
import com.milelog.retrofit.response.GetDrivingStatisticsResponse
import com.milelog.retrofit.response.GetRecentDrivingStatisticsResponse
import com.milelog.retrofit.response.GraphItem
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import java.time.format.DateTimeFormatter

class DrivenTimeActivity: BaseRefreshActivity() {
    lateinit var btn_back:ImageView
    lateinit var layout_barchart_time:BarChart
    lateinit var layout_linechart_time:LineChart
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView

    lateinit var tv_time_info1:TextView
    lateinit var tv_time_info2:TextView
    lateinit var tv_time_info3:TextView
    lateinit var tv_time_info4:TextView

    lateinit var tv_hour:TextView
    lateinit var tv_minute:TextView
    lateinit var tv_average_hour:TextView
    lateinit var tv_average_minute:TextView
    lateinit var tv_max_hour:TextView
    lateinit var tv_max_minute:TextView
    lateinit var tv_min_hour:TextView
    lateinit var tv_min_minute:TextView
    lateinit var tv_diff_time:TextView

    lateinit var tv_date1:TextView
    lateinit var tv_date2:TextView
    lateinit var tv_date3:TextView


    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_time)

        init()
        setResources()

        setRecentDrivingTime()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_time = findViewById(R.id.layout_barchart_time)
        layout_linechart_time = findViewById(R.id.layout_linechart_time)

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        tv_time_info1 = findViewById(R.id.tv_drive_time_info1)
        tv_time_info2 = findViewById(R.id.tv_drive_time_info2)
        tv_time_info3 = findViewById(R.id.tv_drive_time_info3)
        tv_time_info4 = findViewById(R.id.tv_drive_time_info4)
        tv_hour = findViewById(R.id.tv_hour)
        tv_minute = findViewById(R.id.tv_minute)
        tv_average_hour = findViewById(R.id.tv_average_hour)
        tv_average_minute = findViewById(R.id.tv_average_minute)
        tv_max_hour = findViewById(R.id.tv_max_hour)
        tv_max_minute = findViewById(R.id.tv_max_minute)
        tv_min_hour = findViewById(R.id.tv_min_hour)
        tv_min_minute = findViewById(R.id.tv_min_minute)
        tv_diff_time = findViewById(R.id.tv_diff_time)

        tv_date1 = findViewById(R.id.tv_date1)
        tv_date2 = findViewById(R.id.tv_date2)
        tv_date3 = findViewById(R.id.tv_date3)

        btn_recent_drive.isSelected = true
    }

    private fun setResources(){
        btn_back.setOnClickListener { finish() }

        btn_recent_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                setRecentDrivingTime()

                btn_recent_drive.isSelected = true
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false            }

        })

        btn_month_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callMonthChart()
                setMonthDrivingTime()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = true
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false            }

        })

        btn_six_month_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callSixMonthChart()
                setSixMonthDrivingTime()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = true
                btn_year_drive.isSelected = false            }

        })

        btn_year_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callYearChart()
                setYearDrivingTime()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = true            }

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

        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)

        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }


    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChart(items : List<GraphItem>) {
        var max = 0

        for(item in items){
            if(secondsToMinutes(item.time) > max)
                max = secondsToMinutes(item.time).toInt()
        }

        if(max == 0){
            setRecentBarChartAsDefault()
            return
        }


        val time = FloatArray(24) { 0f }

        // Iterate over each item and parse the startTime to extract the hour
        for (item in items) {
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val offsetDateTime = OffsetDateTime.parse(item.startTime, formatter)

            // Convert OffsetDateTime to Instant
            val startTime = offsetDateTime.toInstant()

            // Convert Instant to ZonedDateTime in UTC
            val utcDateTime = ZonedDateTime.ofInstant(startTime, ZoneId.of("UTC"))

            // Extract the hour from ZonedDateTime
            val hour = utcDateTime.hour

            time[hour] = secondsToMinutes(item.time).toFloat()
        }

        val entries = listOf(
            BarEntry(-1f, time.get(0)), // 00시
            BarEntry(-0f, time.get(1)), // 01시
            BarEntry(1f, time.get(2)), // 02시
            BarEntry(2f, time.get(3)), // 03시
            BarEntry(3f, time.get(4)), // 04시
            BarEntry(4f, time.get(5)), // 05시
            BarEntry(5f, time.get(6)), // 06시
            BarEntry(6f, time.get(7)), // 07시
            BarEntry(7f, time.get(8)), // 08시
            BarEntry(8f, time.get(9)), // 09시
            BarEntry(9f, time.get(10)), // 10시
            BarEntry(10f, time.get(11)), // 11시
            BarEntry(11f, time.get(12)), // 12시
            BarEntry(12f, time.get(13)), // 13시
            BarEntry(13f, time.get(14)), // 14시
            BarEntry(14f, time.get(15)), // 15시
            BarEntry(15f, time.get(16)), // 16시
            BarEntry(16f, time.get(17)), // 17시
            BarEntry(17f, time.get(18)), // 18시
            BarEntry(18f, time.get(19)), // 19시
            BarEntry(19f, time.get(20)), // 20시
            BarEntry(20f,time.get(21)), // 21시
            BarEntry(21f,time.get(22)), // 22시
            BarEntry(22f,time.get(23)) // 23시
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.granularity = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        rightAxis.granularity = 1f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)


        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
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

        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }

    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart(items : List<GraphItem>, dates:List<String>) {

        var max = 0

        for(item in items){
            if(secondsToMinutes(item.time) > max)
                max = secondsToMinutes(item.time).toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault(dates)
            return
        }

        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(items.get(0).time).toFloat()),
            BarEntry(-0f, secondsToMinutes(items.get(1).time).toFloat()),
            BarEntry(1f, secondsToMinutes(items.get(2).time).toFloat()),
            BarEntry(2f, secondsToMinutes(items.get(3).time).toFloat()),
            BarEntry(3f, secondsToMinutes(items.get(4).time).toFloat()),
            BarEntry(4f, secondsToMinutes(items.get(5).time).toFloat()),
            BarEntry(5f, secondsToMinutes(items.get(6).time).toFloat()),
            BarEntry(6f, secondsToMinutes(items.get(7).time).toFloat()),
            BarEntry(7f, secondsToMinutes(items.get(8).time).toFloat()),
            BarEntry(8f, secondsToMinutes(items.get(9).time).toFloat()),
            BarEntry(9f, secondsToMinutes(items.get(10).time).toFloat()),
            BarEntry(10f, secondsToMinutes(items.get(11).time).toFloat()),
            BarEntry(11f, secondsToMinutes(items.get(12).time).toFloat()),
            BarEntry(12f, secondsToMinutes(items.get(13).time).toFloat()),
            BarEntry(13f, secondsToMinutes(items.get(14).time).toFloat()),
            BarEntry(14f, secondsToMinutes(items.get(15).time).toFloat()),
            BarEntry(15f, secondsToMinutes(items.get(16).time).toFloat()),
            BarEntry(16f, secondsToMinutes(items.get(17).time).toFloat()),
            BarEntry(17f, secondsToMinutes(items.get(18).time).toFloat()),
            BarEntry(18f, secondsToMinutes(items.get(19).time).toFloat()),
            BarEntry(19f, secondsToMinutes(items.get(20).time).toFloat()),
            BarEntry(20f,secondsToMinutes(items.get(21).time).toFloat()),
            BarEntry(21f,secondsToMinutes(items.get(22).time).toFloat()),
            BarEntry(22f,secondsToMinutes(items.get(23).time).toFloat()),
            BarEntry(23f,secondsToMinutes(items.get(24).time).toFloat()),
            BarEntry(24f,secondsToMinutes(items.get(25).time).toFloat()),
            BarEntry(25f,secondsToMinutes(items.get(26).time).toFloat()),
            BarEntry(26f,secondsToMinutes(items.get(27).time).toFloat()),
            BarEntry(27f,secondsToMinutes(items.get(28).time).toFloat()),
            BarEntry(28f,secondsToMinutes(items.get(29).time).toFloat())
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
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
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }

    private fun callMonthChart(){
        apiService().getDrivingTimeGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.USER_CARID, "")!!,
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

                if(response.code() == 200 || response.code() == 201){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setMonthBarChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(29).third)
                    setMonthLineChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(29).third)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    private fun callSixMonthChart(){

        apiService().getDrivingTimeGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.USER_CARID, "")!!,
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

                if(response.code() == 200 || response.code() == 201){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setSixMonthBarChart(getDrivingGraphDataResponse.items,getCurrentAndPastTimeForISO(150).third )
                    setSixMonthLineChart(getDrivingGraphDataResponse.items,getCurrentAndPastTimeForISO(150).third )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    private fun callYearChart(){
        apiService().getDrivingTimeGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.USER_CARID, "")!!,
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

                if(response.code() == 200 || response.code() == 201){
                    val getDrivingGraphDataResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingGraphDataResponse::class.java
                    )

                    setYearBarChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(334).third)
                    setYearLineChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(334).third)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + ""// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }

    /**
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChart(items : List<GraphItem>, months:List<String>) {

        var max = 0

        for(item in items){
            if(secondsToMinutes(item.time) > max)
                max = secondsToMinutes(item.time).toInt()
        }

        if(max == 0){
            setSixMonthBarChartAsDefault(months)
            return
        }


        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(items.get(0).time).toFloat()), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, secondsToMinutes(items.get(1).time).toFloat()), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, secondsToMinutes(items.get(2).time).toFloat()), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, secondsToMinutes(items.get(3).time).toFloat()), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, secondsToMinutes(items.get(4).time).toFloat()), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, secondsToMinutes(items.get(5).time).toFloat()) // 여섯번째 월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
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
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
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
        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }

    private fun setYearBarChart(items : List<GraphItem>, months:List<String>) {
        var max = 0

        for(item in items){
            if(secondsToMinutes(item.time) > max)
                max = secondsToMinutes(item.time).toInt()
        }

        if(max == 0){
            setYearBarChartAsDefault(months)
            return
        }


        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(items.get(0).time).toFloat()), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, secondsToMinutes(items.get(1).time).toFloat()), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, secondsToMinutes(items.get(2).time).toFloat()), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, secondsToMinutes(items.get(3).time).toFloat()), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, secondsToMinutes(items.get(4).time).toFloat()), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, secondsToMinutes(items.get(5).time).toFloat()), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, secondsToMinutes(items.get(6).time).toFloat()), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, secondsToMinutes(items.get(7).time).toFloat()), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, secondsToMinutes(items.get(8).time).toFloat()), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, secondsToMinutes(items.get(9).time).toFloat()), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, secondsToMinutes(items.get(10).time).toFloat()), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,secondsToMinutes(items.get(11).time).toFloat()) // 12월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 1.0f
        layout_barchart_time.data = barData
        layout_barchart_time.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_time.description.isEnabled = false
        layout_barchart_time.animateY(1000)
        layout_barchart_time.legend.isEnabled = false
        layout_barchart_time.setTouchEnabled(false)
        layout_barchart_time.setExtraOffsets(0f,0f,20f,0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_time.xAxis
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
        val leftAxis = layout_barchart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_time.axisRight
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
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_time.invalidate() // refresh
    }


    /**
     * 최근 주행 (24개 데이터) -> 0시간 ~ 23시간
     * xAxis.valueFormatter -> 오전 12시 , 오전 6시, 오후 12시, 오후 6시
     *
     * 1개월 (28 / 30 / 31개 데이터) -> 해당 월의 일
     * 각 월요일 표기
     * 4개 or 5개 표기
     *
     * 6개월 (6개 데이터) -> 6개월
     * 6개 표기
     *
     * 1년 (12개 데이터) -> 1월 ~ 12월
     * 4개 표기
     */
    private fun setRecentLineChartAsDefault() {
        // 데이터 준비
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

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
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
                    22-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setRecentLineChart(items: List<GraphItem>) {

        var max = 0.0

        for(item in items){
            max += secondsToMinutes(item.time).toDouble()
        }

        if(max == 0.0){
            setRecentLineChartAsDefault()
            return
        }

        val time = DoubleArray(24) { 0.0 }

        for (item in items) {
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val offsetDateTime = OffsetDateTime.parse(item.startTime, formatter)

            // Convert OffsetDateTime to Instant
            val startTime = offsetDateTime.toInstant()

            // Convert Instant to ZonedDateTime in UTC
            val utcDateTime = ZonedDateTime.ofInstant(startTime, ZoneId.of("UTC"))

            // Extract the hour from ZonedDateTime
            val hour = utcDateTime.hour

            time[hour] = secondsToMinutes(item.time)
        }


        val entries = listOf(
            BarEntry(-1f, time.sliceArray(0..0).sum().toFloat()), // 00시
            BarEntry(-0f, time.sliceArray(0..1).sum().toFloat()), // 01시
            BarEntry(1f, time.sliceArray(0..2).sum().toFloat()), // 02시
            BarEntry(2f, time.sliceArray(0..3).sum().toFloat()), // 03시
            BarEntry(3f, time.sliceArray(0..4).sum().toFloat()), // 04시
            BarEntry(4f, time.sliceArray(0..5).sum().toFloat()), // 05시
            BarEntry(5f, time.sliceArray(0..6).sum().toFloat()), // 06시
            BarEntry(6f, time.sliceArray(0..7).sum().toFloat()), // 07시
            BarEntry(7f, time.sliceArray(0..8).sum().toFloat()), // 08시
            BarEntry(8f, time.sliceArray(0..9).sum().toFloat()), // 09시
            BarEntry(9f, time.sliceArray(0..10).sum().toFloat()), // 10시
            BarEntry(10f, time.sliceArray(0..11).sum().toFloat()), // 11시
            BarEntry(11f, time.sliceArray(0..12).sum().toFloat()), // 12시
            BarEntry(12f, time.sliceArray(0..13).sum().toFloat()), // 13시
            BarEntry(13f, time.sliceArray(0..14).sum().toFloat()), // 14시
            BarEntry(14f, time.sliceArray(0..15).sum().toFloat()), // 15시
            BarEntry(15f, time.sliceArray(0..16).sum().toFloat()), // 16시
            BarEntry(16f, time.sliceArray(0..17).sum().toFloat()), // 17시
            BarEntry(17f, time.sliceArray(0..18).sum().toFloat()), // 18시
            BarEntry(18f, time.sliceArray(0..19).sum().toFloat()), // 19시
            BarEntry(19f, time.sliceArray(0..20).sum().toFloat()), // 20시
            BarEntry(20f,time.sliceArray(0..21).sum().toFloat()), // 21시
            BarEntry(21f,time.sliceArray(0..22).sum().toFloat()), // 22시
            BarEntry(22f,time.sliceArray(0..23).sum().toFloat()) // 23시
        )

        Log.d("testsetestset","testestset :: " + time.sliceArray(0..23).sum())

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setMonthLineChartAsDefault(months: List<String>) {
        // 데이터 준비
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
            BarEntry(28f,0f),
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setMonthLineChart(items: List<GraphItem>, months: List<String>) {
        var max = 0.0

        for(item in items){
            max += secondsToMinutes(item.time).toDouble()
        }

        if(max == 0.0){
            setMonthLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.time
        }


        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(distances.sliceArray(0..0).sum()).toFloat()),
            BarEntry(-0f, secondsToMinutes(distances.sliceArray(0..1).sum()).toFloat()),
            BarEntry(1f, secondsToMinutes(distances.sliceArray(0..2).sum()).toFloat()),
            BarEntry(2f, secondsToMinutes(distances.sliceArray(0..3).sum()).toFloat()),
            BarEntry(3f, secondsToMinutes(distances.sliceArray(0..4).sum()).toFloat()),
            BarEntry(4f, secondsToMinutes(distances.sliceArray(0..5).sum()).toFloat()),
            BarEntry(5f, secondsToMinutes(distances.sliceArray(0..6).sum()).toFloat()),
            BarEntry(6f, secondsToMinutes(distances.sliceArray(0..7).sum()).toFloat()),
            BarEntry(7f, secondsToMinutes(distances.sliceArray(0..8).sum()).toFloat()),
            BarEntry(8f, secondsToMinutes(distances.sliceArray(0..9).sum()).toFloat()),
            BarEntry(9f, secondsToMinutes(distances.sliceArray(0..10).sum()).toFloat()),
            BarEntry(10f, secondsToMinutes(distances.sliceArray(0..11).sum()).toFloat()),
            BarEntry(11f, secondsToMinutes(distances.sliceArray(0..12).sum()).toFloat()),
            BarEntry(12f, secondsToMinutes(distances.sliceArray(0..13).sum()).toFloat()),
            BarEntry(13f, secondsToMinutes(distances.sliceArray(0..14).sum()).toFloat()),
            BarEntry(14f, secondsToMinutes(distances.sliceArray(0..15).sum()).toFloat()),
            BarEntry(15f, secondsToMinutes(distances.sliceArray(0..16).sum()).toFloat()),
            BarEntry(16f, secondsToMinutes(distances.sliceArray(0..17).sum()).toFloat()),
            BarEntry(17f, secondsToMinutes(distances.sliceArray(0..18).sum()).toFloat()),
            BarEntry(18f, secondsToMinutes(distances.sliceArray(0..19).sum()).toFloat()),
            BarEntry(19f, secondsToMinutes(distances.sliceArray(0..20).sum()).toFloat()),
            BarEntry(20f,secondsToMinutes(distances.sliceArray(0..21).sum()).toFloat()),
            BarEntry(21f,secondsToMinutes(distances.sliceArray(0..22).sum()).toFloat()),
            BarEntry(22f,secondsToMinutes(distances.sliceArray(0..23).sum()).toFloat()),
            BarEntry(23f,secondsToMinutes(distances.sliceArray(0..24).sum()).toFloat()),
            BarEntry(24f,secondsToMinutes(distances.sliceArray(0..25).sum()).toFloat()),
            BarEntry(25f,secondsToMinutes(distances.sliceArray(0..26).sum()).toFloat()),
            BarEntry(26f,secondsToMinutes(distances.sliceArray(0..27).sum()).toFloat()),
            BarEntry(27f,secondsToMinutes(distances.sliceArray(0..28).sum()).toFloat()),
            BarEntry(28f,secondsToMinutes(distances.sliceArray(0..29).sum()).toFloat())
        )


        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setSixMonthLineChartAsDefault(months: List<String>) {
        // 데이터 준비
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
            BarEntry(21f,0f)
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 10f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 12

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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 80f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setSixMonthLineChart(items:List<GraphItem>, months: List<String>) {
        var max = 0.0

        for(item in items){
            max += secondsToMinutes(item.time).toDouble()
        }

        if(max == 0.0){
            setSixMonthLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.time
        }

        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(distances.sliceArray(0..0).sum()).toFloat()), // 첫번째 월
            BarEntry(0f, secondsToMinutes(distances.sliceArray(0..0).sum()).toFloat()),
            BarEntry(1f, secondsToMinutes(distances.sliceArray(0..1).sum()).toFloat()), // 두번째 월
            BarEntry(2f, secondsToMinutes(distances.sliceArray(0..1).sum()).toFloat()),
            BarEntry(3f, secondsToMinutes(distances.sliceArray(0..2).sum()).toFloat()), // 세번째 월
            BarEntry(4f, secondsToMinutes(distances.sliceArray(0..2).sum()).toFloat()),
            BarEntry(5f, secondsToMinutes(distances.sliceArray(0..3).sum()).toFloat()), // 네번째 월
            BarEntry(6f, secondsToMinutes(distances.sliceArray(0..3).sum()).toFloat()),
            BarEntry(7f, secondsToMinutes(distances.sliceArray(0..4).sum()).toFloat()), // 다섯번째 월
            BarEntry(8f, secondsToMinutes(distances.sliceArray(0..4).sum()).toFloat()),
            BarEntry(9f, secondsToMinutes(distances.sliceArray(0..5).sum()).toFloat()) // 여섯번째 월
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 10f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 12

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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setYearLineChartAsDefault(months: List<String>) {
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
        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
        xAxis.axisMinimum = -2f
        xAxis.axisMaximum = 22f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)
        xAxis.labelCount = 24

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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 80f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }

    private fun setYearLineChart(items: List<GraphItem>, months: List<String>) {
        var max = 0.0

        for(item in items){
            max += secondsToMinutes(item.time).toDouble()
        }

        if(max == 0.0){
            setYearLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.time
        }

        val entries = listOf(
            BarEntry(-1f, secondsToMinutes(distances.sliceArray(0..0).sum()).toFloat()), // 1월
            BarEntry(-0f, secondsToMinutes(distances.sliceArray(0..0).sum()).toFloat()),
            BarEntry(1f, secondsToMinutes(distances.sliceArray(0..1).sum()).toFloat()), // 2월
            BarEntry(2f, secondsToMinutes(distances.sliceArray(0..1).sum()).toFloat()),
            BarEntry(3f, secondsToMinutes(distances.sliceArray(0..2).sum()).toFloat()), // 3월
            BarEntry(4f, secondsToMinutes(distances.sliceArray(0..2).sum()).toFloat()),
            BarEntry(5f, secondsToMinutes(distances.sliceArray(0..3).sum()).toFloat()), // 4월
            BarEntry(6f, secondsToMinutes(distances.sliceArray(0..3).sum()).toFloat()),
            BarEntry(7f, secondsToMinutes(distances.sliceArray(0..4).sum()).toFloat()), // 5월
            BarEntry(8f, secondsToMinutes(distances.sliceArray(0..4).sum()).toFloat()),
            BarEntry(9f, secondsToMinutes(distances.sliceArray(0..5).sum()).toFloat()), // 6월
            BarEntry(10f, secondsToMinutes(distances.sliceArray(0..5).sum()).toFloat()),
            BarEntry(11f, secondsToMinutes(distances.sliceArray(0..6).sum()).toFloat()), // 7월
            BarEntry(12f, secondsToMinutes(distances.sliceArray(0..6).sum()).toFloat()),
            BarEntry(13f, secondsToMinutes(distances.sliceArray(0..7).sum()).toFloat()), // 8월
            BarEntry(14f, secondsToMinutes(distances.sliceArray(0..7).sum()).toFloat()),
            BarEntry(15f, secondsToMinutes(distances.sliceArray(0..8).sum()).toFloat()), // 9월
            BarEntry(16f, secondsToMinutes(distances.sliceArray(0..8).sum()).toFloat()),
            BarEntry(17f, secondsToMinutes(distances.sliceArray(0..9).sum()).toFloat()), // 10월
            BarEntry(18f, secondsToMinutes(distances.sliceArray(0..9).sum()).toFloat()),
            BarEntry(19f, secondsToMinutes(distances.sliceArray(0..10).sum()).toFloat()), // 11월
            BarEntry(20f,secondsToMinutes(distances.sliceArray(0..10).sum()).toFloat()),
            BarEntry(21f,secondsToMinutes(distances.sliceArray(0..11).sum()).toFloat()) // 12월
        )
        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.LINEAR // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillDrawable = getDrawable(R.drawable.line_chart_gradient)

        // 데이터셋 리스트 생성
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        // LineData 객체 생성
        val lineData = LineData(dataSets)

        // LineChart 설정
        layout_linechart_time.data = lineData // 데이터 설정
        layout_linechart_time.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_time.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_time.legend.isEnabled = false
        layout_linechart_time.setTouchEnabled(false)
        layout_linechart_time.setExtraOffsets(20f,0f,20f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_time.xAxis
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
        val leftAxis = layout_linechart_time.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_time.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        rightAxis.granularity = 1.0f
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum

                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "분"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_time.invalidate()
    }


    private fun setRecentDrivingTime(){
        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val recentDrivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetRecentDrivingStatisticsResponse::class.java
                    )

                    tv_date1.text = convertDateFormat(recentStartTime)
                    tv_date2.text = convertDateFormat(recentStartTime)
                    tv_date3.text = convertDateFormat(recentStartTime)

                    if(recentDrivingDistance.isRecent){
                        tv_diff_time.visibility = View.VISIBLE

                        tv_hour.text = transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(recentDrivingDistance.average.totalTime).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(recentDrivingDistance.average.totalTime).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(recentDrivingDistance.min.totalTime).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(recentDrivingDistance.min.totalTime).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(recentDrivingDistance.max.totalTime).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(recentDrivingDistance.max.totalTime).second.toString()

                        if(recentDrivingDistance.diffAverage.totalTime == 0.0){
                            tv_diff_time.text = "시간 변동이 없어요."
                            tv_diff_time.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(recentDrivingDistance.diffAverage.totalTime > 0.0){
                            tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(recentDrivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(recentDrivingDistance.diffAverage.totalTime).second + "분 증가"
                            tv_diff_time.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(recentDrivingDistance.diffAverage.totalTime < 0.0){
                            tv_diff_time.text = transferSecondsToHourAndMinutes(recentDrivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(recentDrivingDistance.diffAverage.totalTime).second + "분 감소"
                            tv_diff_time.setTextColor(resources.getColor(R.color.sec_500))
                        }


                        recentStartTime = recentDrivingDistance.recentStartTime
                        recentEndTime = recentDrivingDistance.recentEndTime

                        tv_time_info1.text = "최근 1일 총합"
                        tv_time_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_time_info3.text = "최근 1일의 기록을\n한눈에 확인해 보세요!"

                        tv_date1.text = convertDateFormat(recentStartTime)
                        tv_date2.text = convertDateFormat(recentStartTime)
                        tv_date3.text = convertDateFormat(recentStartTime)

                        tv_time_info4.text = CommonUtil.getSpannableString(
                            this@DrivenTimeActivity,
                            "최근 1일간 내 차는\n" + transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).first +"시간" + transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).second + "분" + " 달렸어요",
                            transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).first.toString() +"시간" + transferSecondsToHourAndMinutes(recentDrivingDistance.total.totalTime).second + "분",
                            resources.getColor(R.color.pri_500)
                        )

                        apiService().getDrivingTimeGraphData(
                            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
                            PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.USER_CARID, "")!!,
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
                                    setRecentLineChart(getDrivingGraphDataResponse.items)
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            }

                        })

                    }else{
                        tv_date1.text = getTodayFormattedDate()
                        tv_date2.text = getTodayFormattedDate()
                        tv_date3.text = getTodayFormattedDate()

                        tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                        tv_time_info4.text = "최근 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"
                        tv_diff_time.visibility = View.INVISIBLE


                        tv_time_info1.text = "최근 1일 총합"
                        tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                        setRecentBarChartAsDefault()
                        setRecentLineChartAsDefault()
                    }
                }else{
                    tv_date1.text = getTodayFormattedDate()
                    tv_date2.text = getTodayFormattedDate()
                    tv_date3.text = getTodayFormattedDate()

                    tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                    tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                    tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                    tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                    tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                    tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                    tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                    tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                    tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                    tv_time_info4.text = "최근 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"
                    tv_diff_time.visibility = View.INVISIBLE


                    tv_time_info1.text = "최근 1일 총합"
                    tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                    setRecentBarChartAsDefault()
                    setRecentLineChartAsDefault()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_diff_time.text = "+" +transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                tv_diff_time.visibility = View.INVISIBLE


                tv_time_info1.text = "최근 1일 총합"
                tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                setRecentBarChartAsDefault()
                setRecentLineChartAsDefault()
            }

        })



    }

    private fun setMonthDrivingTime(){
        tv_date1.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        tv_date2.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        tv_date3.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                    if(drivingDistance.total.totalTime != 0.0){
                        tv_diff_time.visibility = View.VISIBLE


                        tv_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).second.toString()


                        if(drivingDistance.diffAverage.totalTime == 0.0){
                            tv_diff_time.text = "시간 변동이 없어요."
                            tv_diff_time.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalTime > 0.0){
                            tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 증가"
                            tv_diff_time.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalTime < 0.0){
                            tv_diff_time.text = transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 감소"
                            tv_diff_time.setTextColor(resources.getColor(R.color.sec_500))
                        }


                        tv_time_info1.text = "일일 평균"
                        tv_time_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_time_info3.text = "최근 1개월의 기록을\n한눈에 확인해 보세요!"

                        tv_date1.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
                        tv_date2.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
                        tv_date3.text = formatDateRangeForAMonth(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)

                        tv_time_info4.text = CommonUtil.getSpannableString(
                            this@DrivenTimeActivity,
                            "최근 1개월간 내 차는\n" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분" + " 달렸어요",
                            transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first.toString() +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분",
                            resources.getColor(R.color.pri_500)
                        )
                    }else{
                        tv_diff_time.visibility = View.INVISIBLE


                        tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                        tv_time_info4.text = "최근 1개월 간 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"

                        tv_time_info1.text = "일일 평균"
                        tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                tv_time_info4.text = "최근 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"

                tv_diff_time.visibility = View.INVISIBLE


                tv_time_info1.text = "일일 평균"
                tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }

    private fun setSixMonthDrivingTime(){

        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
        tv_date3.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                    if(drivingDistance.total.totalTime != 0.0){
                        tv_diff_time.visibility = View.VISIBLE


                        tv_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).second.toString()

                        if(drivingDistance.diffAverage.totalTime == 0.0){
                            tv_diff_time.text = "시간 변동이 없어요."
                            tv_diff_time.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalTime > 0.0){
                            tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 증가"
                            tv_diff_time.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalTime < 0.0){
                            tv_diff_time.text = transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 감소"
                            tv_diff_time.setTextColor(resources.getColor(R.color.sec_500))
                        }


                        tv_time_info1.text = "월 평균"
                        tv_time_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_time_info3.text = "최근 6개월의 기록을\n한눈에 확인해 보세요!"

                        tv_time_info4.text = CommonUtil.getSpannableString(
                            this@DrivenTimeActivity,
                            "최근 6개월간 내 차는\n" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분" + " 달렸어요",
                            transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first.toString() +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분",
                            resources.getColor(R.color.pri_500)
                        )
                    }else{
                        tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                        tv_diff_time.visibility = View.INVISIBLE

                        tv_time_info1.text = "월 평균"
                        tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                tv_diff_time.visibility = View.INVISIBLE

                tv_time_info1.text = "월 평균"
                tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }

    private fun setYearDrivingTime(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
        tv_date3.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenTimeActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                    if(drivingDistance.total.totalTime != 0.0){
                        tv_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(drivingDistance.average.totalTime).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(drivingDistance.min.totalTime).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(drivingDistance.max.totalTime).second.toString()

                        tv_diff_time.visibility = View.VISIBLE

                        if(drivingDistance.diffAverage.totalTime == 0.0){
                            tv_diff_time.text = "시간 변동이 없어요."
                            tv_diff_time.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalTime > 0.0){
                            tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 증가"
                            tv_diff_time.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalTime < 0.0){
                            tv_diff_time.text = transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).first.toString() + "시간 " + transferSecondsToHourAndMinutes(drivingDistance.diffAverage.totalTime).second + "분 감소"
                            tv_diff_time.setTextColor(resources.getColor(R.color.sec_500))
                        }


                        tv_time_info1.text = "월 평균"
                        tv_time_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_time_info3.text = "최근 1년의 기록을\n한눈에 확인해 보세요!"

                        tv_time_info4.text = CommonUtil.getSpannableString(
                            this@DrivenTimeActivity,
                            "최근 1년간 내 차는\n" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분" + " 달렸어요",
                            transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).first.toString() +"시간" + transferSecondsToHourAndMinutes(drivingDistance.total.totalTime).second + "분",
                            resources.getColor(R.color.pri_500)
                        )
                    }else{
                        tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                        tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                        tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                        tv_time_info4.text = "최근 1년간 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"

                        tv_diff_time.visibility = View.INVISIBLE

                        tv_time_info1.text = "월 평균"
                        tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_average_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_average_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_min_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_min_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_max_hour.text = transferSecondsToHourAndMinutes(0.0).first.toString()
                tv_max_minute.text = transferSecondsToHourAndMinutes(0.0).second.toString()
                tv_diff_time.text = "+" + transferSecondsToHourAndMinutes(0.0).first.toString() + "시간 " + transferSecondsToHourAndMinutes(0.0).second + "분 증가"

                tv_diff_time.visibility = View.INVISIBLE

                tv_time_info4.text = "최근 내 차는\n" + transferSecondsToHourAndMinutes(0.0).first +"시간" + transferSecondsToHourAndMinutes(0.0).second + "분" + " 달렸어요"

                tv_time_info1.text = "월 평균"
                tv_time_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_time_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }
}