package com.milelog.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.response.GetDrivingGraphDataResponse
import com.milelog.retrofit.response.GetDrivingStatisticsResponse
import com.milelog.retrofit.response.GetRecentDrivingStatisticsResponse
import com.milelog.retrofit.response.GraphItem
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.milelog.GaScreenName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import java.time.format.DateTimeFormatter

class AverageDrivenDistanceActivity: BaseRefreshActivity() {
    lateinit var btn_back:ImageView
    lateinit var layout_barchart_distance:BarChart
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView

    lateinit var tv_driving_info1:TextView
    lateinit var tv_driving_info2:TextView
    lateinit var tv_driving_info3:TextView

    lateinit var tv_total_distance:TextView
    lateinit var tv_total_distance_unit:TextView

    lateinit var tv_average_distance:TextView
    lateinit var tv_average_distance_unit:TextView
    lateinit var tv_max_distance:TextView
    lateinit var tv_max_distance_unit:TextView
    lateinit var tv_min_distance:TextView
    lateinit var tv_min_distance_unit:TextView
    lateinit var tv_diff_distance:TextView

    lateinit var tv_date1:TextView
    lateinit var tv_date2:TextView

    var recentStartTime = "2024-07-15T00:00:00.000Z"
    var recentEndTime = "2024-07-15T23:59:59.999Z"

    lateinit var userCarId:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_average_driven_distance)

        init()
        setResources()

        setRecentDrivingDistance()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(GaScreenName.SCREEN_AVERAGE_DRIVING_DISTANCE_PER, this::class.java.simpleName)
    }



    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_distance = findViewById(R.id.layout_barchart_distance)

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        tv_driving_info1 = findViewById(R.id.tv_driving_info1)
        tv_driving_info2 = findViewById(R.id.tv_driving_info2)
        tv_driving_info3 = findViewById(R.id.tv_driving_info3)
        tv_total_distance = findViewById(R.id.tv_total_distance)
        tv_total_distance_unit = findViewById(R.id.tv_total_distance_unit)
        tv_average_distance = findViewById(R.id.tv_average_distance)
        tv_average_distance_unit = findViewById(R.id.tv_average_distance_unit)
        tv_max_distance = findViewById(R.id.tv_max_distance)
        tv_max_distance_unit = findViewById(R.id.tv_max_distance_unit)
        tv_min_distance = findViewById(R.id.tv_min_distance)
        tv_min_distance_unit = findViewById(R.id.tv_min_distance_unit)
        tv_diff_distance = findViewById(R.id.tv_diff_distance)

        tv_total_distance_unit.text = distance_unit
        tv_average_distance_unit.text = distance_unit
        tv_max_distance_unit.text = distance_unit
        tv_min_distance_unit.text = distance_unit

        tv_date1 = findViewById(R.id.tv_date1)
        tv_date2 = findViewById(R.id.tv_date2)


        btn_recent_drive.isSelected = true

        userCarId = intent.getStringExtra("userCarId")?:PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.USER_CARID, "")!!
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
        apiService().getDrivingDistancePerOneGraphData(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
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
                try {

                    if (response.code() == 200 || response.code() == 201) {
                        val getDrivingGraphDataResponse = Gson().fromJson(
                            response.body()?.string(),
                            GetDrivingGraphDataResponse::class.java
                        )

                        setMonthBarChart(
                            getDrivingGraphDataResponse.items,
                            getCurrentAndPastTimeForISO(29).third
                        )
                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun callSixMonthChart(){
        apiService().getDrivingDistancePerOneGraphData(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(SIX_MONTH).second,
            getCurrentAndPastTimeForISO(SIX_MONTH).first,
            "startTime",
            "month"
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getDrivingGraphDataResponse = Gson().fromJson(
                            response.body()?.string(),
                            GetDrivingGraphDataResponse::class.java
                        )

                        setSixMonthBarChart(
                            getDrivingGraphDataResponse.items,
                            getCurrentAndPastTimeForISO(SIX_MONTH).third
                        )
                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun callYearChart(){
        apiService().getDrivingDistancePerOneGraphData(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            "ASC",
            null,
            null,
            getCurrentAndPastTimeForISO(YEAR).second,
            getCurrentAndPastTimeForISO(YEAR).first,
            "startTime",
            "month"
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val getDrivingGraphDataResponse = Gson().fromJson(
                            response.body()?.string(),
                            GetDrivingGraphDataResponse::class.java
                        )

                        setYearBarChart(
                            getDrivingGraphDataResponse.items,
                            getCurrentAndPastTimeForISO(YEAR).third
                        )
                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

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

    private fun setResources(){
        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }

        })

        btn_recent_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                setRecentDrivingDistance()

                btn_recent_drive.isSelected = true
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false            }

        })

        btn_month_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callMonthChart()
                setMonthDrivingDistance()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = true
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false            }

        })

        btn_six_month_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callSixMonthChart()
                setSixMonthDrivingDistance()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = true
                btn_year_drive.isSelected = false            }

        })

        btn_year_drive.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                callYearChart()
                setYearDrivingDistance()

                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = true            }

        })
    }

    private fun setRecentDrivingDistance(){
        tv_date1.text = convertDateFormat(recentStartTime)
        tv_date2.text = convertDateFormat(recentStartTime)

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        val recentDrivingDistance = Gson().fromJson(
                            response.body()?.string(),
                            GetRecentDrivingStatisticsResponse::class.java
                        )

                        if (recentDrivingDistance.isRecent) {
                            tv_diff_distance.visibility = View.VISIBLE

                            tv_total_distance.text =
                                transferDistance(recentDrivingDistance.perOneAverage.totalDistance)
                            tv_average_distance.text =
                                transferDistance(recentDrivingDistance.perOneAverage.totalDistance)
                            tv_min_distance.text =
                                transferDistance(recentDrivingDistance.perOneMin.totalDistance)
                            tv_max_distance.text =
                                transferDistance(recentDrivingDistance.perOneMax.totalDistance)


                            if (recentDrivingDistance.diffPerOneAverage.totalDistance == 0.0) {
                                tv_diff_distance.text = "점수 변동이 없어요."
                                tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                            } else if (recentDrivingDistance.diffPerOneAverage.totalDistance > 0.0) {
                                tv_diff_distance.text =
                                    "+" + transferDistance(recentDrivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 증가"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.pri_800))

                            } else if (recentDrivingDistance.diffPerOneAverage.totalDistance < 0.0) {
                                tv_diff_distance.text =
                                    transferDistance(recentDrivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 감소"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                            }

                            recentStartTime = recentDrivingDistance.recentStartTime
                            recentEndTime = recentDrivingDistance.recentEndTime

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
                            tv_driving_info3.text = "최근 1일의 기록을\n한눈에 확인해 보세요!"


                            tv_date1.text = convertDateFormat(recentStartTime)
                            tv_date2.text = convertDateFormat(recentStartTime)

                            apiService().getDrivingDistancePerOneGraphData(
                                "Bearer " + PreferenceUtil.getPref(
                                    this@AverageDrivenDistanceActivity,
                                    PreferenceUtil.ACCESS_TOKEN,
                                    ""
                                )!!,
                                userCarId,
                                "ASC",
                                null,
                                null,
                                recentStartTime,
                                recentEndTime,
                                "startTime",
                                "hour"
                            ).enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>
                                ) {
                                    try {

                                        if (response.code() == 200 || response.code() == 201) {
                                            val getDrivingGraphDataResponse = Gson().fromJson(
                                                response.body()?.string(),
                                                GetDrivingGraphDataResponse::class.java
                                            )


                                            setRecentBarChart(getDrivingGraphDataResponse.items)
                                        }else if(response.code() == 401){
                                            logout()
                                        }
                                    }catch (e:Exception){

                                    }
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                                }

                            })

                        } else {
                            tv_date1.text = getTodayFormattedDate()
                            tv_date2.text = getTodayFormattedDate()

                            tv_total_distance.text = transferDistance(0.0)
                            tv_diff_distance.text =
                                "+" + transferDistance(0.0) + distance_unit + " 증가"
                            tv_average_distance.text = transferDistance(0.0)
                            tv_max_distance.text = transferDistance(0.0)
                            tv_min_distance.text = transferDistance(0.0)

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                            tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                            setRecentBarChartAsDefault()

                            tv_diff_distance.visibility = View.INVISIBLE

                        }
                    } else if(response.code() == 401){
                        logout()
                    } else {
                        tv_date1.text = getTodayFormattedDate()
                        tv_date2.text = getTodayFormattedDate()

                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)

                        tv_driving_info1.text = "1회 평균"
                        tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                        tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                        setRecentBarChartAsDefault()

                        tv_diff_distance.visibility = View.INVISIBLE
                    }
                }catch (e:Exception){

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_driving_info1.text = "1회 평균"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                setRecentBarChartAsDefault()

                tv_diff_distance.visibility = View.INVISIBLE

            }

        })



    }

    private fun setMonthDrivingDistance(){
        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {

                        val drivingDistance = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetDrivingStatisticsResponse::class.java
                        )

                        if (drivingDistance.total.totalDistance != 0.0) {

                            tv_diff_distance.visibility = View.VISIBLE

                            tv_date1.text = formatDateRangeForAMonth(
                                getCurrentAndPastTimeForISO(29).second,
                                getCurrentAndPastTimeForISO(29).first
                            )
                            tv_date2.text = formatDateRangeForAMonth(
                                getCurrentAndPastTimeForISO(29).second,
                                getCurrentAndPastTimeForISO(29).first
                            )

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
                            tv_driving_info3.text = "최근 1개월의 기록을\n한눈에 확인해 보세요!"

                            tv_total_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_average_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_max_distance.text =
                                transferDistance(drivingDistance.perOneMax.totalDistance)
                            tv_min_distance.text =
                                transferDistance(drivingDistance.perOneMin.totalDistance)

                            if (drivingDistance.diffPerOneAverage.totalDistance == 0.0) {
                                tv_diff_distance.text = "점수 변동이 없어요."
                                tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance > 0.0) {
                                tv_diff_distance.text =
                                    "+" + transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 증가"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.pri_800))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance < 0.0) {
                                tv_diff_distance.text =
                                    transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 감소"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                            }
                        } else {
                            tv_total_distance.text = transferDistance(0.0)
                            tv_diff_distance.text =
                                "+" + transferDistance(0.0) + distance_unit + " 증가"
                            tv_average_distance.text = transferDistance(0.0)
                            tv_max_distance.text = transferDistance(0.0)
                            tv_min_distance.text = transferDistance(0.0)

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                            tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                            tv_diff_distance.visibility = View.INVISIBLE

                            setRecentBarChartAsDefault()
                        }
                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_driving_info1.text = "1회 평균"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                setRecentBarChartAsDefault()

                tv_diff_distance.visibility = View.INVISIBLE

            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_date1.text = formatDateRange(getCurrentAndPastTimeForISO(SIX_MONTH).second,getCurrentAndPastTimeForISO(SIX_MONTH).first)
        tv_date2.text = formatDateRange(getCurrentAndPastTimeForISO(SIX_MONTH).second,getCurrentAndPastTimeForISO(SIX_MONTH).first)

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(SIX_MONTH).second,
            getCurrentAndPastTimeForISO(SIX_MONTH).first,
            "startTime",
            "").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {

                        val drivingDistance = GsonBuilder().serializeNulls().create().fromJson(
                            response.body()?.string(),
                            GetDrivingStatisticsResponse::class.java
                        )


                        if (drivingDistance.average.totalDistance != 0.0) {
                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
                            tv_driving_info3.text = "최근 6개월의 기록을\n한눈에 확인해 보세요!"

                            tv_total_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_average_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_max_distance.text =
                                transferDistance(drivingDistance.perOneMax.totalDistance)
                            tv_min_distance.text =
                                transferDistance(drivingDistance.perOneMin.totalDistance)

                            if (drivingDistance.total.totalDistance == 0.0) {
                                tv_diff_distance.text = "점수 변동이 없어요."
                                tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance > 0.0) {
                                tv_diff_distance.text =
                                    "+" + transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 증가"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.pri_800))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance < 0.0) {
                                tv_diff_distance.text =
                                    transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 감소"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                            }

                            tv_diff_distance.visibility = View.VISIBLE


                        } else {
                            tv_total_distance.text = transferDistance(0.0)
                            tv_diff_distance.text =
                                "+" + transferDistance(0.0) + distance_unit + " 증가"
                            tv_average_distance.text = transferDistance(0.0)
                            tv_max_distance.text = transferDistance(0.0)
                            tv_min_distance.text = transferDistance(0.0)

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                            tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                            setRecentBarChartAsDefault()

                            tv_diff_distance.visibility = View.INVISIBLE

                        }
                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_driving_info1.text = "1회 평균"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                setRecentBarChartAsDefault()

                tv_diff_distance.visibility = View.INVISIBLE

            }

        })
    }

    private fun setYearDrivingDistance(){
        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            userCarId,
            getCurrentAndPastTimeForISO(YEAR).second,
            getCurrentAndPastTimeForISO(YEAR).first,
            "startTime",
            "").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.code() == 200 || response.code() == 201) {

                        val drivingDistance = Gson().fromJson(
                            response.body()?.string(),
                            GetDrivingStatisticsResponse::class.java
                        )

                        tv_diff_distance.visibility = View.VISIBLE

                        if (drivingDistance.total.totalDistance != 0.0) {
                            if (drivingDistance.diffPerOneAverage.totalDistance == 0.0) {
                                tv_diff_distance.text = "점수 변동이 없어요."
                                tv_diff_distance.setTextColor(resources.getColor(R.color.gray_950))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance > 0.0) {
                                tv_diff_distance.text =
                                    "+" + transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 증가"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.pri_800))

                            } else if (drivingDistance.diffPerOneAverage.totalDistance < 0.0) {
                                tv_diff_distance.text =
                                    transferDistance(drivingDistance.diffPerOneAverage.totalDistance) + distance_unit + " 감소"
                                tv_diff_distance.setTextColor(resources.getColor(R.color.sec_500))
                            }

                            tv_total_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_average_distance.text =
                                transferDistance(drivingDistance.perOneAverage.totalDistance)
                            tv_max_distance.text =
                                transferDistance(drivingDistance.perOneMax.totalDistance)
                            tv_min_distance.text =
                                transferDistance(drivingDistance.perOneMin.totalDistance)

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
                            tv_driving_info3.text = "최근 1년의 기록을\n한눈에 확인해 보세요!"

                            tv_date1.text = formatDateRange(
                                getCurrentAndPastTimeForISO(YEAR).second,
                                getCurrentAndPastTimeForISO(YEAR).first
                            )
                            tv_date2.text = formatDateRange(
                                getCurrentAndPastTimeForISO(YEAR).second,
                                getCurrentAndPastTimeForISO(YEAR).first
                            )
                        } else {
                            tv_total_distance.text = transferDistance(0.0)
                            tv_diff_distance.text =
                                "+" + transferDistance(0.0) + distance_unit + " 증가"
                            tv_average_distance.text = transferDistance(0.0)
                            tv_max_distance.text = transferDistance(0.0)
                            tv_min_distance.text = transferDistance(0.0)

                            tv_driving_info1.text = "1회 평균"
                            tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                            tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                            setRecentBarChartAsDefault()

                            tv_diff_distance.visibility = View.INVISIBLE
                        }


                    }else if(response.code() == 401){
                        logout()
                    }
                }catch (e:Exception){

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_distance.text = transferDistance(0.0)
                tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                tv_average_distance.text = transferDistance(0.0)
                tv_max_distance.text = transferDistance(0.0)
                tv_min_distance.text = transferDistance(0.0)

                tv_driving_info1.text = "1회 평균"
                tv_driving_info2.text = "아직 데이터가 없어요.\n함께 달려볼까요?"
                tv_driving_info3.text = "아직 데이터가 없어요.\n함께 달려볼까요?"

                tv_diff_distance.visibility = View.INVISIBLE


                setRecentBarChartAsDefault()
            }

        })
    }
}