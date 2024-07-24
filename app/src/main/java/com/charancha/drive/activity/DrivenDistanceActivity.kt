package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.CommonUtil
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.*
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

class DrivenDistanceActivity:BaseRefreshActivity() {
    lateinit var btn_back:ImageView
    lateinit var layout_barchart_distance:BarChart
    lateinit var layout_linechart_distance:LineChart
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView
    lateinit var tv_total_distance:TextView
    lateinit var tv_diff_distance:TextView
    lateinit var tv_average_distance:TextView
    lateinit var tv_min_distance:TextView
    lateinit var tv_max_distance:TextView
    lateinit var tv_total_distance_unit:TextView
    lateinit var tv_average_distance_unit:TextView
    lateinit var tv_max_distance_unit:TextView
    lateinit var tv_min_distance_unit:TextView

    lateinit var tv_driving_info1:TextView
    lateinit var tv_driving_info2:TextView
    lateinit var tv_driving_info3:TextView
    lateinit var tv_driving_info4:TextView

    lateinit var tv_date1:TextView
    lateinit var tv_date2:TextView
    lateinit var tv_date3:TextView


    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_distance)

        init()
        setResources()

        setRecentDrivingDistance()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_distance = findViewById(R.id.layout_barchart_distance)
        layout_linechart_distance = findViewById(R.id.layout_linechart_distance)

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        tv_total_distance = findViewById(R.id.tv_total_distance)
        tv_diff_distance = findViewById(R.id.tv_diff_distance)
        tv_average_distance = findViewById(R.id.tv_average_distance)
        tv_min_distance = findViewById(R.id.tv_min_distance)
        tv_max_distance = findViewById(R.id.tv_max_distance)

        tv_total_distance_unit = findViewById(R.id.tv_total_distance_unit)
        tv_average_distance_unit = findViewById(R.id.tv_average_distance_unit)
        tv_max_distance_unit = findViewById(R.id.tv_max_distance_unit)
        tv_min_distance_unit = findViewById(R.id.tv_min_distance_unit)

        tv_total_distance_unit.text = distance_unit
        tv_average_distance_unit.text = distance_unit
        tv_max_distance_unit.text = distance_unit
        tv_min_distance_unit.text = distance_unit

        tv_driving_info1 = findViewById(R.id.tv_driving_info1)
        tv_driving_info2 = findViewById(R.id.tv_driving_info2)
        tv_driving_info3 = findViewById(R.id.tv_driving_info3)
        tv_driving_info4 = findViewById(R.id.tv_driving_info4)

        tv_date1 = findViewById(R.id.tv_date1)
        tv_date2 = findViewById(R.id.tv_date2)
        tv_date3 = findViewById(R.id.tv_date3)



        btn_recent_drive.isSelected = true
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

        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
    }


    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChart(items : List<GraphItem>) {
        var max = 0

        for(item in items){
            if(transferDistance(item.totalDistance).toDouble() > max.toDouble())
                max = transferDistance(item.totalDistance).toDouble().toInt()
        }

        if(max == 0){
            setRecentBarChartAsDefault()
            return
        }

        val distances = FloatArray(24) { 0f }

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

            distances[hour] = transferDistance(item.totalDistance).toFloat()
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

        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
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

        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
    }

    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart(items : List<GraphItem>, dates:List<String>) {

        var max = 0

        for(item in items){
            if(transferDistance(item.totalDistance).toDouble() > max.toDouble())
                max = transferDistance(item.totalDistance).toDouble().toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault(dates)
            return
        }

        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).totalDistance).toFloat()),
            BarEntry(-0f, transferDistance(items.get(1).totalDistance).toFloat()),
            BarEntry(1f, transferDistance(items.get(2).totalDistance).toFloat()),
            BarEntry(2f, transferDistance(items.get(3).totalDistance).toFloat()),
            BarEntry(3f, transferDistance(items.get(4).totalDistance).toFloat()),
            BarEntry(4f, transferDistance(items.get(5).totalDistance).toFloat()),
            BarEntry(5f, transferDistance(items.get(6).totalDistance).toFloat()),
            BarEntry(6f, transferDistance(items.get(7).totalDistance).toFloat()),
            BarEntry(7f, transferDistance(items.get(8).totalDistance).toFloat()),
            BarEntry(8f, transferDistance(items.get(9).totalDistance).toFloat()),
            BarEntry(9f, transferDistance(items.get(10).totalDistance).toFloat()),
            BarEntry(10f, transferDistance(items.get(11).totalDistance).toFloat()),
            BarEntry(11f, transferDistance(items.get(12).totalDistance).toFloat()),
            BarEntry(12f, transferDistance(items.get(13).totalDistance).toFloat()),
            BarEntry(13f, transferDistance(items.get(14).totalDistance).toFloat()),
            BarEntry(14f, transferDistance(items.get(15).totalDistance).toFloat()),
            BarEntry(15f, transferDistance(items.get(16).totalDistance).toFloat()),
            BarEntry(16f, transferDistance(items.get(17).totalDistance).toFloat()),
            BarEntry(17f, transferDistance(items.get(18).totalDistance).toFloat()),
            BarEntry(18f, transferDistance(items.get(19).totalDistance).toFloat()),
            BarEntry(19f, transferDistance(items.get(20).totalDistance).toFloat()),
            BarEntry(20f,transferDistance(items.get(21).totalDistance).toFloat()),
            BarEntry(21f,transferDistance(items.get(22).totalDistance).toFloat()),
            BarEntry(22f,transferDistance(items.get(23).totalDistance).toFloat()),
            BarEntry(23f,transferDistance(items.get(24).totalDistance).toFloat()),
            BarEntry(24f,transferDistance(items.get(25).totalDistance).toFloat()),
            BarEntry(25f,transferDistance(items.get(26).totalDistance).toFloat()),
            BarEntry(26f,transferDistance(items.get(27).totalDistance).toFloat()),
            BarEntry(27f,transferDistance(items.get(28).totalDistance).toFloat()),
            BarEntry(28f,transferDistance(items.get(29).totalDistance).toFloat())
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
    }

    private fun callMonthChart(){
        Log.d("testestestests","testeststesset :: " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!)
        Log.d("testestestests","testeststesset :: " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!)
        Log.d("testestestests","testeststesset :: " + getCurrentAndPastTimeForISO(29).second)
        Log.d("testestestests","testeststesset :: " + getCurrentAndPastTimeForISO(29).first)

        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
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
                    setMonthLineChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(29).third)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun callSixMonthChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
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
                    setSixMonthLineChart(getDrivingGraphDataResponse.items,getCurrentAndPastTimeForISO(150).third )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun callYearChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
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
        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
    }

    /**
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChart(items : List<GraphItem>, months:List<String>) {

        var max = 0

        for(item in items){
            if(transferDistance(item.totalDistance).toDouble() > max.toDouble())
                max = transferDistance(item.totalDistance).toDouble().toInt()
        }

        if(max == 0){
            setSixMonthBarChartAsDefault(months)
            return
        }


        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).totalDistance).toFloat()), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, transferDistance(items.get(1).totalDistance).toFloat()), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, transferDistance(items.get(2).totalDistance).toFloat()), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, transferDistance(items.get(3).totalDistance).toFloat()), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, transferDistance(items.get(4).totalDistance).toFloat()), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, transferDistance(items.get(5).totalDistance).toFloat()) // 여섯번째 월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
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
        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
    }

    private fun setYearBarChart(items : List<GraphItem>, months:List<String>) {
        var max = 0

        for(item in items){
            if(transferDistance(item.totalDistance).toDouble() > max.toDouble())
                max = transferDistance(item.totalDistance).toDouble().toInt()
        }

        if(max == 0){
            setYearBarChartAsDefault(months)
            return
        }



        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).totalDistance).toFloat()), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, transferDistance(items.get(1).totalDistance).toFloat()), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, transferDistance(items.get(2).totalDistance).toFloat()), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, transferDistance(items.get(3).totalDistance).toFloat()), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, transferDistance(items.get(4).totalDistance).toFloat()), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, transferDistance(items.get(5).totalDistance).toFloat()), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, transferDistance(items.get(6).totalDistance).toFloat()), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, transferDistance(items.get(7).totalDistance).toFloat()), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, transferDistance(items.get(8).totalDistance).toFloat()), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, transferDistance(items.get(9).totalDistance).toFloat()), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, transferDistance(items.get(10).totalDistance).toFloat()), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,transferDistance(items.get(11).totalDistance).toFloat()) // 12월
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.setDrawValues(false) // 막대 위의 값을 표시하지 않도록 설정

        val barData = BarData(dataSet)
        barData.barWidth = 1.0f
        layout_barchart_distance.data = barData
        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false
        layout_barchart_distance.setTouchEnabled(false)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
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
        val leftAxis = layout_barchart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 5로 설정 (강제)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_distance.axisRight
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

        layout_barchart_distance.invalidate() // refresh
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setRecentLineChart(items: List<GraphItem>) {

        var max = 0.0

        for(item in items){
            max += transferDistance(item.totalDistance).toDouble()
        }

        if(max == 0.0){
            setRecentLineChartAsDefault()
            return
        }


        val distances = FloatArray(24) { 0f }

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

            distances[hour] = transferDistance(item.totalDistance).toFloat()
        }


        val entries = listOf(
            BarEntry(-1f, distances.sliceArray(0..0).sum()), // 00시
            BarEntry(-0f, distances.sliceArray(0..1).sum()), // 01시
            BarEntry(1f, distances.sliceArray(0..2).sum()), // 02시
            BarEntry(2f, distances.sliceArray(0..3).sum()), // 03시
            BarEntry(3f, distances.sliceArray(0..4).sum()), // 04시
            BarEntry(4f, distances.sliceArray(0..5).sum()), // 05시
            BarEntry(5f, distances.sliceArray(0..6).sum()), // 06시
            BarEntry(6f, distances.sliceArray(0..7).sum()), // 07시
            BarEntry(7f, distances.sliceArray(0..8).sum()), // 08시
            BarEntry(8f, distances.sliceArray(0..9).sum()), // 09시
            BarEntry(9f, distances.sliceArray(0..10).sum()), // 10시
            BarEntry(10f, distances.sliceArray(0..11).sum()), // 11시
            BarEntry(11f, distances.sliceArray(0..12).sum()), // 12시
            BarEntry(12f, distances.sliceArray(0..13).sum()), // 13시
            BarEntry(13f, distances.sliceArray(0..14).sum()), // 14시
            BarEntry(14f, distances.sliceArray(0..15).sum()), // 15시
            BarEntry(15f, distances.sliceArray(0..16).sum()), // 16시
            BarEntry(16f, distances.sliceArray(0..17).sum()), // 17시
            BarEntry(17f, distances.sliceArray(0..18).sum()), // 18시
            BarEntry(18f, distances.sliceArray(0..19).sum()), // 19시
            BarEntry(19f, distances.sliceArray(0..20).sum()), // 20시
            BarEntry(20f,distances.sliceArray(0..21).sum()), // 21시
            BarEntry(21f,distances.sliceArray(0..22).sum()), // 22시
            BarEntry(22f,distances.sliceArray(0..23).sum()) // 23시
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setMonthLineChart(items: List<GraphItem>, months: List<String>) {
        var max = 0.0

        for(item in items){
            max += transferDistance(item.totalDistance).toDouble()
        }

        if(max == 0.0){
            setMonthLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.totalDistance
        }


        val entries = listOf(
            BarEntry(-1f, transferDistance(distances.sliceArray(0..0).sum()).toFloat()),
            BarEntry(-0f, transferDistance(distances.sliceArray(0..1).sum()).toFloat()),
            BarEntry(1f, transferDistance(distances.sliceArray(0..2).sum()).toFloat()),
            BarEntry(2f, transferDistance(distances.sliceArray(0..3).sum()).toFloat()),
            BarEntry(3f, transferDistance(distances.sliceArray(0..4).sum()).toFloat()),
            BarEntry(4f, transferDistance(distances.sliceArray(0..5).sum()).toFloat()),
            BarEntry(5f, transferDistance(distances.sliceArray(0..6).sum()).toFloat()),
            BarEntry(6f, transferDistance(distances.sliceArray(0..7).sum()).toFloat()),
            BarEntry(7f, transferDistance(distances.sliceArray(0..8).sum()).toFloat()),
            BarEntry(8f, transferDistance(distances.sliceArray(0..9).sum()).toFloat()),
            BarEntry(9f, transferDistance(distances.sliceArray(0..10).sum()).toFloat()),
            BarEntry(10f, transferDistance(distances.sliceArray(0..11).sum()).toFloat()),
            BarEntry(11f, transferDistance(distances.sliceArray(0..12).sum()).toFloat()),
            BarEntry(12f, transferDistance(distances.sliceArray(0..13).sum()).toFloat()),
            BarEntry(13f, transferDistance(distances.sliceArray(0..14).sum()).toFloat()),
            BarEntry(14f, transferDistance(distances.sliceArray(0..15).sum()).toFloat()),
            BarEntry(15f, transferDistance(distances.sliceArray(0..16).sum()).toFloat()),
            BarEntry(16f, transferDistance(distances.sliceArray(0..17).sum()).toFloat()),
            BarEntry(17f, transferDistance(distances.sliceArray(0..18).sum()).toFloat()),
            BarEntry(18f, transferDistance(distances.sliceArray(0..19).sum()).toFloat()),
            BarEntry(19f, transferDistance(distances.sliceArray(0..20).sum()).toFloat()),
            BarEntry(20f,transferDistance(distances.sliceArray(0..21).sum()).toFloat()),
            BarEntry(21f,transferDistance(distances.sliceArray(0..22).sum()).toFloat()),
            BarEntry(22f,transferDistance(distances.sliceArray(0..23).sum()).toFloat()),
            BarEntry(23f,transferDistance(distances.sliceArray(0..24).sum()).toFloat()),
            BarEntry(24f,transferDistance(distances.sliceArray(0..25).sum()).toFloat()),
            BarEntry(25f,transferDistance(distances.sliceArray(0..26).sum()).toFloat()),
            BarEntry(26f,transferDistance(distances.sliceArray(0..27).sum()).toFloat()),
            BarEntry(27f,transferDistance(distances.sliceArray(0..28).sum()).toFloat()),
            BarEntry(28f,transferDistance(distances.sliceArray(0..29).sum()).toFloat())
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setSixMonthLineChart(items:List<GraphItem>,months: List<String>) {
        var max = 0.0

        for(item in items){
            max += transferDistance(item.totalDistance).toDouble()
        }

        if(max == 0.0){
            setSixMonthLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.totalDistance
        }


        val entries = listOf(
            BarEntry(-1f, transferDistance(distances.sliceArray(0..0).sum()).toFloat()), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f,  transferDistance(distances.sliceArray(0..1).sum()).toFloat()), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f,  transferDistance(distances.sliceArray(0..2).sum()).toFloat()), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f,  transferDistance(distances.sliceArray(0..3).sum()).toFloat()), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f,  transferDistance(distances.sliceArray(0..4).sum()).toFloat()), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f,  transferDistance(distances.sliceArray(0..5).sum()).toFloat()) // 여섯번째 월
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setYearLineChart(items: List<GraphItem>, months: List<String>) {
        var max = 0.0

        for(item in items){
            max += transferDistance(item.totalDistance).toDouble()
        }

        if(max == 0.0){
            setYearLineChartAsDefault(months)
            return
        }

        val distances = DoubleArray(items.size) { 0.0 }

        for((index,item) in items.withIndex()){
            distances[index] = item.totalDistance
        }



        val entries = listOf(
            BarEntry(-1f, transferDistance(distances.sliceArray(0..0).sum()).toFloat()), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, transferDistance(distances.sliceArray(0..1).sum()).toFloat()), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, transferDistance(distances.sliceArray(0..2).sum()).toFloat()), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, transferDistance(distances.sliceArray(0..3).sum()).toFloat()), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, transferDistance(distances.sliceArray(0..4).sum()).toFloat()), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, transferDistance(distances.sliceArray(0..5).sum()).toFloat()), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, transferDistance(distances.sliceArray(0..6).sum()).toFloat()), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, transferDistance(distances.sliceArray(0..7).sum()).toFloat()), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, transferDistance(distances.sliceArray(0..8).sum()).toFloat()), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, transferDistance(distances.sliceArray(0..9).sum()).toFloat()), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, transferDistance(distances.sliceArray(0..10).sum()).toFloat()), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,transferDistance(distances.sliceArray(0..11).sum()).toFloat()) // 12월
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
        layout_linechart_distance.data = lineData // 데이터 설정
        layout_linechart_distance.setDrawGridBackground(false) // 그리드 배경 그리기 여부 설정
        layout_linechart_distance.description.isEnabled = false // 설명 텍스트 사용 여부 설정
        layout_linechart_distance.legend.isEnabled = false
        layout_linechart_distance.setTouchEnabled(false)
        layout_linechart_distance.setExtraOffsets(20f,0f,0f,0f)


        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
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
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(5, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = max.toFloat()
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
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
                    value.toInt().toString() + distance_unit// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setRecentDrivingDistance(){
        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!).enqueue(object:Callback<ResponseBody>{
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
                        recentStartTime = recentDrivingDistance.recentStartTime
                        recentEndTime = recentDrivingDistance.recentEndTime

                        tv_date1.text = convertDateFormat(recentStartTime)
                        tv_date2.text = convertDateFormat(recentStartTime)
                        tv_date3.text = convertDateFormat(recentStartTime)

                        tv_total_distance.text = transferDistance(recentDrivingDistance.total.totalDistance)
                        tv_diff_distance.visibility = VISIBLE

                        if(recentDrivingDistance.diffTotal.totalDistance == 0.0){
                            tv_diff_distance.text = "점수 변동이 없어요."
                            tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(recentDrivingDistance.diffTotal.totalDistance > 0.0){
                            tv_diff_distance.text = "+" + transferDistance(recentDrivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(recentDrivingDistance.diffTotal.totalDistance < 0.0){
                            tv_diff_distance.text = transferDistance(recentDrivingDistance.diffTotal.totalDistance) + distance_unit + " 감소"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_average_distance.text = transferDistance(recentDrivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(recentDrivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(recentDrivingDistance.min.totalDistance)

                        tv_driving_info1.text = "최근 1일 총합"
                        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_driving_info3.text = "최근 1일의 기록을\n한눈에 확인해 보세요!"
                        tv_driving_info4.text = "최근 1일간 내 차는\n" + transferDistance(recentDrivingDistance.total.totalDistance) + distance_unit + " 달렸어요"

                        tv_driving_info4.text = CommonUtil.getSpannableString(
                            this@DrivenDistanceActivity,
                            "최근 1일간 내 차는\n" + transferDistance(recentDrivingDistance.total.totalDistance) + distance_unit + " 달렸어요",
                            transferDistance(recentDrivingDistance.total.totalDistance) + distance_unit,
                            resources.getColor(R.color.pri_500)
                        )


                        apiService().getDrivingDistanceGraphData(
                            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
                            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
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
                                }else{
                                    setRecentBarChartAsDefault()
                                    setRecentLineChartAsDefault()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                setRecentBarChartAsDefault()
                                setRecentLineChartAsDefault()                            }

                        })


                    }else{
                        setRecentBarChartAsDefault()
                        setRecentLineChartAsDefault()
                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)

                        tv_diff_distance.visibility = INVISIBLE

                        tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                setRecentBarChartAsDefault()
                setRecentLineChartAsDefault()
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_diff_distance.visibility = INVISIBLE

                tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })



    }

    private fun setMonthDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)
        tv_date3.text = formatDateRange(getCurrentAndPastTimeForISO(29).second,getCurrentAndPastTimeForISO(29).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(drivingDistance.average.totalDistance != 0.0){
                        tv_diff_distance.visibility = VISIBLE
                        tv_total_distance.text = transferDistance(drivingDistance.average.totalDistance)

                        if(drivingDistance.diffAverage.totalDistance == 0.0){
                            tv_diff_distance.text = "점수 변동이 없어요."
                            tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalDistance > 0.0){
                            tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 증가"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalDistance < 0.0){
                            tv_diff_distance.text = transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 감소"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                        tv_driving_info1.text = "일일 평균"
                        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_driving_info3.text = "최근 1개월의 기록을\n한눈에 확인해 보세요!"
                        // TextView에 SpannableString 설정
                        tv_driving_info4.text = CommonUtil.getSpannableString(
                            this@DrivenDistanceActivity,
                            "최근 1개월간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요",
                            transferDistance(drivingDistance.total.totalDistance) + distance_unit,
                            resources.getColor(R.color.pri_500)
                        )
                    }else{
                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "점수 변동이 없어요."
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)

                        tv_diff_distance.visibility = INVISIBLE

                        tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }else{
                    tv_total_distance.text = transferDistance(0.0)
                    tv_diff_distance.text = "점수 변동이 없어요."
                    tv_average_distance.text = transferDistance(0.0)
                    tv_max_distance.text = transferDistance(0.0)
                    tv_min_distance.text = transferDistance(0.0)

                    tv_diff_distance.visibility = INVISIBLE

                    tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_diff_distance.visibility = INVISIBLE

                tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }

    private fun setSixMonthDrivingDistance(){

        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)
        tv_date3.text = formatDateRange(getCurrentAndPastTimeForISO(150).second,getCurrentAndPastTimeForISO(150).first)


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(150).second,
            getCurrentAndPastTimeForISO(150).first,
            "startTime",
            "month").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(drivingDistance.average.totalDistance != 0.0){
                        tv_total_distance.text = transferDistance(drivingDistance.average.totalDistance)

                        if(drivingDistance.diffAverage.totalDistance == 0.0){
                            tv_diff_distance.text = "점수 변동이 없어요."
                            tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalDistance > 0.0){
                            tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 증가"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalDistance < 0.0){
                            tv_diff_distance.text = transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 감소"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                        tv_diff_distance.visibility = VISIBLE

                        tv_driving_info1.text = "월 평균"
                        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_driving_info3.text = "최근 6개월의 기록을\n한눈에 확인해 보세요!"
                        tv_driving_info4.text = CommonUtil.getSpannableString(
                            this@DrivenDistanceActivity,
                            "최근 6개월간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요",
                            transferDistance(drivingDistance.total.totalDistance) + distance_unit,
                            resources.getColor(R.color.pri_500)
                        )
                    }
                }else{
                    tv_total_distance.text = transferDistance(0.0)
                    tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                    tv_average_distance.text = transferDistance(0.0)
                    tv_max_distance.text = transferDistance(0.0)
                    tv_min_distance.text = transferDistance(0.0)

                    tv_diff_distance.visibility = INVISIBLE

                    tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_diff_distance.visibility = INVISIBLE

                tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)
        tv_date3.text = formatDateRange(getCurrentAndPastTimeForISO(334).second,getCurrentAndPastTimeForISO(334).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(334).second,
            getCurrentAndPastTimeForISO(334).first,
            "startTime",
            "month").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(drivingDistance.average.totalDistance != 0.0){
                        tv_total_distance.text = transferDistance(drivingDistance.average.totalDistance)

                        if(drivingDistance.diffAverage.totalDistance == 0.0){
                            tv_diff_distance.text = "점수 변동이 없어요."
                            tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                        }else if(drivingDistance.diffAverage.totalDistance > 0.0){
                            tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 증가"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.pri_500))

                        }else if(drivingDistance.diffAverage.totalDistance < 0.0){
                            tv_diff_distance.text = transferDistance(drivingDistance.diffAverage.totalDistance) + distance_unit + " 감소"
                            tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                        }

                        tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                        tv_driving_info1.text = "월 평균"
                        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
                        tv_driving_info3.text = "최근 1년의 기록을\n한눈에 확인해 보세요!"
                        tv_driving_info4.text = "최근 1년 간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요"

                        tv_driving_info4.text = CommonUtil.getSpannableString(
                            this@DrivenDistanceActivity,
                            "최근 1년 간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요",
                            transferDistance(drivingDistance.total.totalDistance) + distance_unit,
                            resources.getColor(R.color.pri_500)
                        )
                    }else{
                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)

                        tv_diff_distance.visibility = INVISIBLE

                        tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_diff_distance.visibility = INVISIBLE

                tv_driving_info1.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
            }

        })
    }

    private fun setResources(){
        btn_back.setOnClickListener { finish() }

        btn_recent_drive.setOnClickListener {
            setRecentDrivingDistance()

            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false
        }

        btn_month_drive.setOnClickListener {
            callMonthChart()
            setMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

        }

        btn_six_month_drive.setOnClickListener {
            callSixMonthChart()
            setSixMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false
        }

        btn_year_drive.setOnClickListener {
            callYearChart()
            setYearDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true
        }
    }
}