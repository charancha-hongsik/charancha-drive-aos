package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DrivenDistanceActivity:BaseActivity() {
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

    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_distance)

        init()
        setResources()

        setRecentBarChartAsDefault()
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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
            if(transferDistance(item.distance).toDouble() > max.toDouble())
                max = transferDistance(item.distance).toDouble().toInt()
        }

        if(max == 0){
            setRecentBarChartAsDefault()
            return
        }

        max++

        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).distance).toFloat()),
            BarEntry(-0f, transferDistance(items.get(1).distance).toFloat()),
            BarEntry(1f, transferDistance(items.get(2).distance).toFloat()),
            BarEntry(2f, transferDistance(items.get(3).distance).toFloat()),
            BarEntry(3f, transferDistance(items.get(4).distance).toFloat()),
            BarEntry(4f, transferDistance(items.get(5).distance).toFloat()),
            BarEntry(5f, transferDistance(items.get(6).distance).toFloat()),
            BarEntry(6f, transferDistance(items.get(7).distance).toFloat()),
            BarEntry(7f, transferDistance(items.get(8).distance).toFloat()),
            BarEntry(8f, transferDistance(items.get(9).distance).toFloat()),
            BarEntry(9f, transferDistance(items.get(10).distance).toFloat()),
            BarEntry(10f, transferDistance(items.get(11).distance).toFloat()),
            BarEntry(11f, transferDistance(items.get(12).distance).toFloat()),
            BarEntry(12f, transferDistance(items.get(13).distance).toFloat()),
            BarEntry(13f, transferDistance(items.get(14).distance).toFloat()),
            BarEntry(14f, transferDistance(items.get(15).distance).toFloat()),
            BarEntry(15f, transferDistance(items.get(16).distance).toFloat()),
            BarEntry(16f, transferDistance(items.get(17).distance).toFloat()),
            BarEntry(17f, transferDistance(items.get(18).distance).toFloat()),
            BarEntry(18f, transferDistance(items.get(19).distance).toFloat()),
            BarEntry(19f, transferDistance(items.get(20).distance).toFloat()),
            BarEntry(20f,transferDistance(items.get(21).distance).toFloat()),
            BarEntry(21f,transferDistance(items.get(22).distance).toFloat()),
            BarEntry(22f,transferDistance(items.get(23).distance).toFloat())
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
                    7 -> "오전 6시"
                    14 -> "오후 12시"
                    20-> "오후 6시"
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
    private fun setMonthBarChartAsDefault() {
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
                    1 -> "6월 17일"
                    10 -> "6월 24일"
                    18 -> "7월 1일"
                    26-> "7월 8일"
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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
            if(transferDistance(item.distance).toDouble() > max.toDouble())
                max = transferDistance(item.distance).toDouble().toInt()
        }

        if(max == 0){
            setMonthBarChartAsDefault()
            return
        }

        max++

        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).distance).toFloat()),
            BarEntry(-0f, transferDistance(items.get(1).distance).toFloat()),
            BarEntry(1f, transferDistance(items.get(2).distance).toFloat()),
            BarEntry(2f, transferDistance(items.get(3).distance).toFloat()),
            BarEntry(3f, transferDistance(items.get(4).distance).toFloat()),
            BarEntry(4f, transferDistance(items.get(5).distance).toFloat()),
            BarEntry(5f, transferDistance(items.get(6).distance).toFloat()),
            BarEntry(6f, transferDistance(items.get(7).distance).toFloat()),
            BarEntry(7f, transferDistance(items.get(8).distance).toFloat()),
            BarEntry(8f, transferDistance(items.get(9).distance).toFloat()),
            BarEntry(9f, transferDistance(items.get(10).distance).toFloat()),
            BarEntry(10f, transferDistance(items.get(11).distance).toFloat()),
            BarEntry(11f, transferDistance(items.get(12).distance).toFloat()),
            BarEntry(12f, transferDistance(items.get(13).distance).toFloat()),
            BarEntry(13f, transferDistance(items.get(14).distance).toFloat()),
            BarEntry(14f, transferDistance(items.get(15).distance).toFloat()),
            BarEntry(15f, transferDistance(items.get(16).distance).toFloat()),
            BarEntry(16f, transferDistance(items.get(17).distance).toFloat()),
            BarEntry(17f, transferDistance(items.get(18).distance).toFloat()),
            BarEntry(18f, transferDistance(items.get(19).distance).toFloat()),
            BarEntry(19f, transferDistance(items.get(20).distance).toFloat()),
            BarEntry(20f,transferDistance(items.get(21).distance).toFloat()),
            BarEntry(21f,transferDistance(items.get(22).distance).toFloat()),
            BarEntry(22f,transferDistance(items.get(23).distance).toFloat()),
            BarEntry(23f,transferDistance(items.get(24).distance).toFloat()),
            BarEntry(24f,transferDistance(items.get(25).distance).toFloat()),
            BarEntry(25f,transferDistance(items.get(26).distance).toFloat()),
            BarEntry(26f,transferDistance(items.get(27).distance).toFloat()),
            BarEntry(27f,transferDistance(items.get(28).distance).toFloat()),
            BarEntry(28f,transferDistance(items.get(29).distance).toFloat())
        )

        for(item in items)
            Log.d("testestsetes","testestestse item.distance :: " + transferDistance(item.distance).toFloat())

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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }

        layout_barchart_distance.invalidate() // refresh
    }

    private fun callMonthBarChart(){
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
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun callSixMonthBarChart(){
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
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun callYearBarChart(){
        apiService().getDrivingDistanceGraphData(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(335).second,
            getCurrentAndPastTimeForISO(335).first,
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

                    setYearBarChart(getDrivingGraphDataResponse.items, getCurrentAndPastTimeForISO(335).third)
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
    private fun setSixMonthBarChartAsDefault() {

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
                    -1 -> "1월"
                    1 -> "2월"
                    3 -> "3월"
                    5 -> "4월"
                    7 -> "5월"
                    9 -> "6월"
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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
            if(transferDistance(item.distance).toDouble() > max.toDouble())
                max = transferDistance(item.distance).toDouble().toInt()
        }

        if(max == 0){
            setSixMonthBarChartAsDefault()
            return
        }

        max++

        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).distance).toFloat()), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, transferDistance(items.get(1).distance).toFloat()), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, transferDistance(items.get(2).distance).toFloat()), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, transferDistance(items.get(3).distance).toFloat()), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, transferDistance(items.get(4).distance).toFloat()), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, transferDistance(items.get(5).distance).toFloat()) // 여섯번째 월
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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

    private fun setYearBarChartAsDefault() {

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
                    -1 -> "1월"
                    7 -> "5월"
                    13 -> "8월"
                    21-> "12월"
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 10f
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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
            if(transferDistance(item.distance).toDouble() > max.toDouble())
                max = transferDistance(item.distance).toDouble().toInt()
        }

        if(max == 0){
            setYearBarChartAsDefault()
            return
        }

        max++


        val entries = listOf(
            BarEntry(-1f, transferDistance(items.get(0).distance).toFloat()), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, transferDistance(items.get(1).distance).toFloat()), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, transferDistance(items.get(2).distance).toFloat()), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, transferDistance(items.get(3).distance).toFloat()), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, transferDistance(items.get(4).distance).toFloat()), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, transferDistance(items.get(5).distance).toFloat()), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, transferDistance(items.get(6).distance).toFloat()), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, transferDistance(items.get(7).distance).toFloat()), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, transferDistance(items.get(8).distance).toFloat()), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, transferDistance(items.get(9).distance).toFloat()), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, transferDistance(items.get(10).distance).toFloat()), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,transferDistance(items.get(11).distance).toFloat()) // 12월
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
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = max.toFloat()
        rightAxis.textColor = getColor(R.color.gray_600)

        // Y축 커스텀 레이블 포매터 설정
        rightAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val minValue = rightAxis.axisMinimum
                val maxValue = rightAxis.axisMaximum
                return if (value == minValue || value == maxValue) {
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
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
    private fun setRecentLineChart() {
        // 데이터 준비
        val entries = listOf(
            BarEntry(-1f, 1f),
            BarEntry(-0f, 3f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 9f),
            BarEntry(4f, 11f),
            BarEntry(5f, 13f),
            BarEntry(6f, 16f),
            BarEntry(7f, 19f),
            BarEntry(8f, 22f),
            BarEntry(9f, 25f),
            BarEntry(10f, 28f),
            BarEntry(11f, 31f),
            BarEntry(12f, 35f),
            BarEntry(13f, 39f),
            BarEntry(14f, 43f),
            BarEntry(15f, 47f),
            BarEntry(16f, 51f),
            BarEntry(17f, 55f),
            BarEntry(18f, 59f),
            BarEntry(19f, 66f),
            BarEntry(20f,73f),
            BarEntry(21f,79f),
            BarEntry(22f,80f)
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선 형태로 설정
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
        leftAxis.setLabelCount(6, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
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
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setMonthLineChart() {
        // 데이터 준비
        val entries = listOf(
            BarEntry(-1f, 1f),
            BarEntry(-0f, 3f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 9f),
            BarEntry(4f, 11f),
            BarEntry(5f, 13f),
            BarEntry(6f, 16f),
            BarEntry(7f, 19f),
            BarEntry(8f, 22f),
            BarEntry(9f, 25f),
            BarEntry(10f, 28f),
            BarEntry(11f, 31f),
            BarEntry(12f, 35f),
            BarEntry(13f, 39f),
            BarEntry(14f, 43f),
            BarEntry(15f, 47f),
            BarEntry(16f, 51f),
            BarEntry(17f, 55f),
            BarEntry(18f, 59f),
            BarEntry(19f, 66f),
            BarEntry(20f,73f),
            BarEntry(21f,79f)
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선 형태로 설정
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
                    1 -> "오전 12시"
                    8 -> "오전 6시"
                    14 -> "오후 12시"
                    20-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(6, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
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
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setSixMonthLineChart() {
        // 데이터 준비
        val entries = listOf(
            BarEntry(-1f, 1f),
            BarEntry(-0f, 3f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 9f),
            BarEntry(4f, 11f),
            BarEntry(5f, 13f),
            BarEntry(6f, 16f),
            BarEntry(7f, 19f),
            BarEntry(8f, 22f),
            BarEntry(9f, 25f),
            BarEntry(10f, 28f),
            BarEntry(11f, 31f),
            BarEntry(12f, 35f),
            BarEntry(13f, 39f),
            BarEntry(14f, 43f),
            BarEntry(15f, 47f),
            BarEntry(16f, 51f),
            BarEntry(17f, 55f),
            BarEntry(18f, 59f),
            BarEntry(19f, 66f),
            BarEntry(20f,73f),
            BarEntry(21f,79f)
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선 형태로 설정
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
                    1 -> "오전 12시"
                    8 -> "오전 6시"
                    14 -> "오후 12시"
                    20-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(6, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
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
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
    }

    private fun setYearLineChart() {
        // 데이터 준비
        val entries = listOf(
            BarEntry(-1f, 1f),
            BarEntry(-0f, 3f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 9f),
            BarEntry(4f, 11f),
            BarEntry(5f, 13f),
            BarEntry(6f, 16f),
            BarEntry(7f, 19f),
            BarEntry(8f, 22f),
            BarEntry(9f, 25f),
            BarEntry(10f, 28f),
            BarEntry(11f, 31f),
            BarEntry(12f, 35f),
            BarEntry(13f, 39f),
            BarEntry(14f, 43f),
            BarEntry(15f, 47f),
            BarEntry(16f, 51f),
            BarEntry(17f, 55f),
            BarEntry(18f, 59f),
            BarEntry(19f, 66f),
            BarEntry(20f,73f),
            BarEntry(21f,79f)
        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선 형태로 설정
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
                    1 -> "오전 12시"
                    7 -> "오전 6시"
                    13 -> "오후 12시"
                    19-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(6, true) // 가로 라인의 수를 6로 설정 (강제)
        leftAxis.granularity = 1.0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 80f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_linechart_distance.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
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
                    value.toInt().toString() + "km"// 가장 아래와 위에만 레이블 표시
                } else {
                    "" // 나머지 레이블 제거
                }
            }
        }
        // 차트 업데이트
        layout_linechart_distance.invalidate()
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
            callMonthBarChart()
            setMonthLineChart()
            setMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

        }

        btn_six_month_drive.setOnClickListener {
            callSixMonthBarChart()
            setSixMonthLineChart()
            setSixMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false
        }

        btn_year_drive.setOnClickListener {
            callYearBarChart()
            setYearLineChart()
            setYearDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true
        }
    }

    private fun setRecentDrivingDistance(){
        tv_driving_info1.text = "최근 1일 주행 거리"
        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
        tv_driving_info3.text = "최근 주행 거리를\n한눈에 확인해보세요!"

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val recentDrivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetRecentDrivingStatisticsResponse::class.java
                    )

                    if(recentDrivingDistance.isRecent){
                        recentStartTime = recentDrivingDistance.recentStartTime
                        recentEndTime = recentDrivingDistance.recentEndTime

                        tv_total_distance.text = transferDistance(recentDrivingDistance.total.totalDistance)
                        tv_diff_distance.visibility = VISIBLE
                        tv_diff_distance.text = "+" + transferDistance(recentDrivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(recentDrivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(recentDrivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(recentDrivingDistance.min.totalDistance)

                        tv_driving_info4.text = "최근 내 차는\n" + transferDistance(recentDrivingDistance.total.totalDistance) + distance_unit + " 달렸어요"

                        apiService().getDrivingDistanceGraphData(
                            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
                            PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!,
                            "ASC",
                            null,
                            null,
                            "2024-07-15T00:00:00.000Z",
                            "2024-07-15T23:59:59.999Z",
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
                                    setRecentLineChart()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                        })


                    }else{
                        tv_diff_distance.visibility = INVISIBLE
                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)

                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info4.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                    }
                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)
            }

        })



    }

    private fun setMonthDrivingDistance(){
        tv_driving_info1.text = "1개월 주행 거리"
        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
        tv_driving_info3.text = "1개월 간 주행 거리를\n한눈에 확인해보세요!"


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(30).second,
            getCurrentAndPastTimeForISO(30).first,
            "startTime",
            "day").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    tv_total_distance.text = transferDistance(drivingDistance.total.totalDistance)
                    tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                    tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                    tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                    tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                    tv_driving_info4.text = "1개월 간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요"
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_driving_info1.text = "6개월 주행 거리"
        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
        tv_driving_info3.text = "6개월 주행 거리를\n한눈에 확인해보세요!"

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

                    tv_total_distance.text = transferDistance(drivingDistance.total.totalDistance)
                    tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                    tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                    tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                    tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                    tv_driving_info4.text = "6개월 간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요"
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_driving_info1.text = "1년 주행 거리"
        tv_driving_info2.text = "내 차는 자주\n달릴수록 좋아요"
        tv_driving_info3.text = "1년 주행 거리를\n한눈에 확인해보세요!"

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@DrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(335).second,
            getCurrentAndPastTimeForISO(335).first,
            "startTime",
            "month").enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200) {

                    val drivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    tv_total_distance.text = transferDistance(drivingDistance.total.totalDistance)
                    tv_diff_distance.text = "+" + transferDistance(drivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                    tv_average_distance.text = transferDistance(drivingDistance.average.totalDistance)
                    tv_max_distance.text = transferDistance(drivingDistance.max.totalDistance)
                    tv_min_distance.text = transferDistance(drivingDistance.min.totalDistance)

                    tv_driving_info4.text = "1년 간 내 차는\n" + transferDistance(drivingDistance.total.totalDistance) + distance_unit + " 달렸어요"
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}