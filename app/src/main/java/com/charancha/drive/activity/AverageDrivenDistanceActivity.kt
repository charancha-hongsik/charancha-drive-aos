package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GetRecentDrivingStatisticsResponse
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

class AverageDrivenDistanceActivity:BaseActivity() {
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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_average_driven_distance)

        init()
        setResources()
        setRecentBarChart()

        setRecentDrivingDistance()
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



        btn_recent_drive.isSelected = true
    }

    /**
     * 24개의 데이터가 내려옴
     */
    private fun setRecentBarChart() {

        val entries = listOf(
            BarEntry(-1f, 6f),
            BarEntry(-0f, 10f),
            BarEntry(1f, 4f),
            BarEntry(2f, 8f),
            BarEntry(3f, 6f),
            BarEntry(4f, 2f),
            BarEntry(5f, 7f),
            BarEntry(6f, 5f),
            BarEntry(7f, 9f),
            BarEntry(8f, 3f),
            BarEntry(9f, 4f),
            BarEntry(10f, 5f),
            BarEntry(11f, 2f),
            BarEntry(12f, 7f),
            BarEntry(13f, 5f),
            BarEntry(14f, 9f),
            BarEntry(15f, 3f),
            BarEntry(16f, 4f),
            BarEntry(17f, 5f),
            BarEntry(18f, 9f),
            BarEntry(19f, 1f),
            BarEntry(20f,2f),
            BarEntry(21f,5f),
            BarEntry(22f,5f)
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
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart() {

        val entries = listOf(
            BarEntry(-1f, 6f),
            BarEntry(-0f, 10f),
            BarEntry(1f, 4f),
            BarEntry(2f, 8f),
            BarEntry(3f, 6f),
            BarEntry(4f, 1f),
            BarEntry(5f, 7f),
            BarEntry(6f, 5f),
            BarEntry(7f, 9f),
            BarEntry(8f, 9f),
            BarEntry(9f, 4f),
            BarEntry(10f, 5f),
            BarEntry(11f, 2f),
            BarEntry(12f, 7f),
            BarEntry(13f, 5f),
            BarEntry(14f, 1f),
            BarEntry(15f, 3f),
            BarEntry(16f, 4f),
            BarEntry(17f, 5f),
            BarEntry(18f, 9f),
            BarEntry(19f, 1f),
            BarEntry(20f,2f),
            BarEntry(21f,5f),
            BarEntry(22f,8f),
            BarEntry(23f,3f),
            BarEntry(24f,4f),
            BarEntry(25f,1f),
            BarEntry(26f,9f),
            BarEntry(27f,5f),
            BarEntry(28f,5f)
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
     * 6개의 데이터가 내려옴
     * 6개 데이터 뿌려주면 됨
     */
    private fun setSixMonthBarChart() {

        val entries = listOf(
            BarEntry(-1f, 6f), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, 8f), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, 7f), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, 3f), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, 2f), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, 9f) // 여섯번째 월
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
     * 데어터가 12개 내려옴 (월 단위의 데이터)
     * 1월 / 5월 / 8월 / 12월
     */

    private fun setYearBarChart() {

        val entries = listOf(
            BarEntry(-1f, 6f), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, 4f), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, 6f), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, 7f), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, 9f), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, 4f), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, 2f), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, 5f), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, 3f), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, 5f), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, 1f), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,5f) // 12월
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

    private fun setResources(){
        btn_back.setOnClickListener { finish() }

        btn_recent_drive.setOnClickListener {
            setRecentBarChart()
            setRecentDrivingDistance()

            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false
        }

        btn_month_drive.setOnClickListener {
            setMonthBarChart()
            setMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

        }

        btn_six_month_drive.setOnClickListener {
            setSixMonthBarChart()
            setSixMonthDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false
        }

        btn_year_drive.setOnClickListener {
            setYearBarChart()
            setYearDrivingDistance()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true
        }
    }

    private fun setRecentDrivingDistance(){
        tv_driving_info1.text = "최근 1일 1회 평균 주행 거리"
        tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
        tv_driving_info3.text = "최근 1회 주행 거리를\n한눈에 확인해보세요!"

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!).enqueue(object:
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val recentDrivingDistance = Gson().fromJson(
                        response.body()?.string(),
                        GetRecentDrivingStatisticsResponse::class.java
                    )

                    if(recentDrivingDistance.isRecent){
                        tv_total_distance.text = transferDistance(recentDrivingDistance.total.totalDistance)
                        tv_diff_distance.text = "+" + transferDistance(recentDrivingDistance.diffTotal.totalDistance) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(recentDrivingDistance.average.totalDistance)
                        tv_max_distance.text = transferDistance(recentDrivingDistance.max.totalDistance)
                        tv_min_distance.text = transferDistance(recentDrivingDistance.min.totalDistance)

                    }else{
                        tv_total_distance.text = transferDistance(0.0)
                        tv_diff_distance.text = "+" + transferDistance(0.0) + distance_unit + " 증가"
                        tv_average_distance.text = transferDistance(0.0)
                        tv_max_distance.text = transferDistance(0.0)
                        tv_min_distance.text = transferDistance(0.0)
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
        tv_driving_info1.text = "1개월 1회 평균 주행 거리"
        tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
        tv_driving_info3.text = "1개월 1회 주행 거리를\n한눈에 확인해보세요!"


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(30).second,
            getCurrentAndPastTimeForISO(30).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
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
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_driving_info1.text = "6개월 1회 평균 주행 거리"
        tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
        tv_driving_info3.text = "6개월 1회 주행 거리를\n한눈에 확인해보세요!"
        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(60).second,
            getCurrentAndPastTimeForISO(60).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
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
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_driving_info1.text = "1년 1회 평균 주행 거리"
        tv_driving_info2.text = "1회 평균 주행 거리는 \n높을수록 좋아요"
        tv_driving_info3.text = "1년 1회 주행 거리를\n한눈에 확인해보세요!"

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@AverageDrivenDistanceActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(365).second,
            getCurrentAndPastTimeForISO(365).first,
            "startTime",
            "day").enqueue(object: Callback<ResponseBody> {
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

                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}