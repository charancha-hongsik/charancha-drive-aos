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
import com.charancha.drive.retrofit.response.GetDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GetRecentDrivingStatisticsResponse
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
import java.util.*

class HighSpeedDrivingActivity:BaseActivity() {
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


    lateinit var layout_barchart_highspeed:BarChart


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

        btn_recent_drive.isSelected = true

        btn_recent_drive.setOnClickListener {
            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            setRecentBarChart()
            setRecentDrivingDistance()

        }

        btn_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            setMonthBarChart()
            setMonthDrivingDistance()
        }

        btn_six_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false

            setSixMonthBarChart()
            setSixMonthDrivingDistance()
        }

        btn_year_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true

            setYearBarChart()
            setYearDrivingDistance()
        }

        setRecentBarChart()
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


    private fun setRecentBarChart() {
        val entries1 = listOf(
            BarEntry(-1f, 3f),
            BarEntry(-0f, 7f),
            BarEntry(1f, 5f),
            BarEntry(2f, 7f),
            BarEntry(3f, 3f),
            BarEntry(4f, 9f),
            BarEntry(5f, 6f),
            BarEntry(6f, 7f),
            BarEntry(7f, 3f),
            BarEntry(8f, 9f),
            BarEntry(9f, 7f),
            BarEntry(10f, 5f),
            BarEntry(11f, 4f),
            BarEntry(12f, 8f),
            BarEntry(13f, 7f),
            BarEntry(14f, 8f),
            BarEntry(15f, 7f),
            BarEntry(16f, 6f),
            BarEntry(17f, 8f),
            BarEntry(18f, 5f),
            BarEntry(19f, 7f),
            BarEntry(20f,4f),
            BarEntry(21f,8f),
            BarEntry(22f,7f)
        )

        val entries2 = listOf(
            BarEntry(-1f, 2f),
            BarEntry(-0f, 5f),
            BarEntry(1f, 3f),
            BarEntry(2f, 5f),
            BarEntry(3f, 2f),
            BarEntry(4f, 6f),
            BarEntry(5f, 5f),
            BarEntry(6f, 6f),
            BarEntry(7f, 2f),
            BarEntry(8f, 6f),
            BarEntry(9f, 5f),
            BarEntry(10f, 4f),
            BarEntry(11f, 2f),
            BarEntry(12f, 4f),
            BarEntry(13f, 4f),
            BarEntry(14f, 6f),
            BarEntry(15f, 4f),
            BarEntry(16f, 5f),
            BarEntry(17f, 4f),
            BarEntry(18f, 2f),
            BarEntry(19f, 6f),
            BarEntry(20f,3f),
            BarEntry(21f,6f),
            BarEntry(22f,4f)
        )

        val entries3 = listOf(
            BarEntry(-1f, 1f),
            BarEntry(-0f, 3f),
            BarEntry(1f, 1f),
            BarEntry(2f, 2f),
            BarEntry(3f, 1f),
            BarEntry(4f, 3f),
            BarEntry(5f, 3f),
            BarEntry(6f, 3f),
            BarEntry(7f, 1f),
            BarEntry(8f, 1f),
            BarEntry(9f, 3f),
            BarEntry(10f, 1f),
            BarEntry(11f, 1f),
            BarEntry(12f, 1f),
            BarEntry(13f, 3f),
            BarEntry(14f, 3f),
            BarEntry(15f, 2f),
            BarEntry(16f, 3f),
            BarEntry(17f, 3f),
            BarEntry(18f, 1f),
            BarEntry(19f, 3f),
            BarEntry(20f,2f),
            BarEntry(21f,3f),
            BarEntry(22f,3f)
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

        layout_barchart_highspeed.invalidate() // refresh

    }

    /**
     * 데이터 30개가 내려옴
     * 각 월요일을 차트 하단에 노출
     */
    private fun setMonthBarChart() {

        val entries1 = listOf(
            BarEntry(-1f, 13f),
            BarEntry(-0f, 10f),
            BarEntry(1f, 15f),
            BarEntry(2f, 10f),
            BarEntry(3f, 16f),
            BarEntry(4f, 11f),
            BarEntry(5f, 19f),
            BarEntry(6f, 9f),
            BarEntry(7f, 15f),
            BarEntry(8f, 10f),
            BarEntry(9f, 11f),
            BarEntry(10f, 12f),
            BarEntry(11f, 14f),
            BarEntry(12f, 3f),
            BarEntry(13f, 5f),
            BarEntry(14f, 8f),
            BarEntry(15f, 9f),
            BarEntry(16f, 10f),
            BarEntry(17f, 9f),
            BarEntry(18f, 13f),
            BarEntry(19f, 9f),
            BarEntry(20f,18f),
            BarEntry(21f,12f),
            BarEntry(22f,10f),
            BarEntry(23f,16f),
            BarEntry(24f,8f),
            BarEntry(25f,5f),
            BarEntry(26f,9f),
            BarEntry(27f,15f),
            BarEntry(28f,9f)
        )

        val entries2 = listOf(
            BarEntry(-1f, 6f),
            BarEntry(-0f, 5f),
            BarEntry(1f, 6f),
            BarEntry(2f, 6f),
            BarEntry(3f, 5f),
            BarEntry(4f, 6f),
            BarEntry(5f, 6f),
            BarEntry(6f, 6f),
            BarEntry(7f, 4f),
            BarEntry(8f, 5f),
            BarEntry(9f, 5f),
            BarEntry(10f, 6f),
            BarEntry(11f, 6f),
            BarEntry(12f, 2f),
            BarEntry(13f, 2f),
            BarEntry(14f, 6f),
            BarEntry(15f, 5f),
            BarEntry(16f, 6f),
            BarEntry(17f, 6f),
            BarEntry(18f, 5f),
            BarEntry(19f, 7f),
            BarEntry(20f,6f),
            BarEntry(21f,6f),
            BarEntry(22f,6f),
            BarEntry(23f,5f),
            BarEntry(24f,6f),
            BarEntry(25f,3f),
            BarEntry(26f,7f),
            BarEntry(27f,6f),
            BarEntry(28f,7f)
        )

        val entries3 = listOf(
            BarEntry(-1f, 3f),
            BarEntry(-0f, 2f),
            BarEntry(1f, 3f),
            BarEntry(2f, 1f),
            BarEntry(3f, 2f),
            BarEntry(4f, 3f),
            BarEntry(5f, 1f),
            BarEntry(6f, 3f),
            BarEntry(7f, 1f),
            BarEntry(8f, 1f),
            BarEntry(9f, 3f),
            BarEntry(10f, 2f),
            BarEntry(11f, 3f),
            BarEntry(12f, 1f),
            BarEntry(13f, 1f),
            BarEntry(14f, 3f),
            BarEntry(15f, 2f),
            BarEntry(16f, 3f),
            BarEntry(17f, 3f),
            BarEntry(18f, 2f),
            BarEntry(19f, 3f),
            BarEntry(20f,3f),
            BarEntry(21f,2f),
            BarEntry(22f,3f),
            BarEntry(23f,1f),
            BarEntry(24f,3f),
            BarEntry(25f,1f),
            BarEntry(26f,1f),
            BarEntry(27f,3f),
            BarEntry(28f,1f)
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
                    1 -> "6월 17일"
                    10 -> "6월 24일"
                    18 -> "7월 1일"
                    26-> "7월 8일"
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
        leftAxis.axisMaximum = 20f
        leftAxis.gridColor = getColor(R.color.gray_200)


        val rightAxis = layout_barchart_highspeed.axisRight
        rightAxis.setDrawGridLines(false) // 그리드 라인 제거
        rightAxis.setDrawAxisLine(false) // 축 라인 제거
        rightAxis.setDrawLabels(true) // Y축 레이블 활성화
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 20f
        rightAxis.labelCount = 21
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

        layout_barchart_highspeed.invalidate() // refresh
    }

    private fun setSixMonthBarChart() {

        val entries1 = listOf(
            BarEntry(-1f, 10f), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, 8f), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, 10f), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, 9f), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, 9f), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, 9f) // 여섯번째 월
        )

        val entries2 = listOf(
            BarEntry(-1f, 6f), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, 6f), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, 7f), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, 6f), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, 6f), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, 7f) // 여섯번째 월
        )

        val entries3 = listOf(
            BarEntry(-1f, 3f), // 첫번째 월
            BarEntry(0f, 0f),
            BarEntry(1f, 1f), // 두번째 월
            BarEntry(2f, 0f),
            BarEntry(3f, 3f), // 세번째 월
            BarEntry(4f, 0f),
            BarEntry(5f, 3f), // 네번째 월
            BarEntry(6f, 0f),
            BarEntry(7f, 2f), // 다섯번째 월
            BarEntry(8f, 0f),
            BarEntry(9f, 1f) // 여섯번째 월
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

        layout_barchart_highspeed.invalidate() // refresh
    }

    private fun setYearBarChart() {

        val entries1 = listOf(
            BarEntry(-1f, 8f), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, 10f), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, 10f), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, 10f), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, 9f), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, 10f), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, 9f), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, 10f), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, 8f), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, 10f), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, 9f), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,9f) // 12월
        )

        val entries2 = listOf(
            BarEntry(-1f, 6f), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, 5f), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, 5f), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, 4f), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, 6f), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, 6f), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, 6f), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, 6f), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, 4f), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, 6f), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, 5f), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,5f) // 12월
        )

        val entries3 = listOf(
            BarEntry(-1f, 2f), // 1월
            BarEntry(-0f, 0f),
            BarEntry(1f, 2f), // 2월
            BarEntry(2f, 0f),
            BarEntry(3f, 2f), // 3월
            BarEntry(4f, 0f),
            BarEntry(5f, 2f), // 4월
            BarEntry(6f, 0f),
            BarEntry(7f, 2f), // 5월
            BarEntry(8f, 0f),
            BarEntry(9f, 2f), // 6월
            BarEntry(10f, 0f),
            BarEntry(11f, 2f), // 7월
            BarEntry(12f, 0f),
            BarEntry(13f, 2f), // 8월
            BarEntry(14f, 0f),
            BarEntry(15f, 3f), // 9월
            BarEntry(16f, 0f),
            BarEntry(17f, 2f), // 10월
            BarEntry(18f, 0f),
            BarEntry(19f, 1f), // 11월
            BarEntry(20f,0f),
            BarEntry(21f,2f) // 12월
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
                    -1 -> "1월"
                    7 -> "5월"
                    13 -> "8월"
                    21-> "12월"
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

        layout_barchart_highspeed.invalidate() // refresh
    }

    private fun setRecentDrivingDistance(){
        tv_driving_info1.text = "최근 평균 고속 주행"
        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
        tv_driving_info3.text = "최근 내 차의\n고속 주행 비율이에요"

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
                        runOnUiThread{
                            tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.highSpeedDrivingDistancePercentage)
                            tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                            tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.highSpeedDrivingDistancePercentage) + "%"
                            tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.lowSpeedDrivingDistancePercentage) + "%"
                            tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", recentDrivingDistance.average.etcSpeedDrivingDistancePercentage) + "%"

                            setHighSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                            setLowSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                            setExtraSpeedDrivingChartWidthByPercent(recentDrivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)
                        }
                    }else{
                        runOnUiThread {
                            tv_total_percent.text = "0.0"
                            tv_diff_percent.text = "+0.0% 증가"
                            tv_high_speed_percent.text = 0.0.toString()
                            tv_low_speed_percent.text = 0.0.toString()
                            tv_etc_speed_percent.text = 0.0.toString()
                        }
                    }
                }else{

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_total_percent.text = "0,0"
                tv_diff_percent.text = "+0.0% 증가"
                tv_high_speed_percent.text = 0.0.toString()
                tv_low_speed_percent.text = 0.0.toString()
                tv_etc_speed_percent.text = 0.0.toString()
            }

        })



    }

    private fun setMonthDrivingDistance(){
        tv_driving_info1.text = "1개월 평균 고속 주행"
        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
        tv_driving_info3.text = "1개월 내 차의\n고속 주행 비율이에요"


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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

                    runOnUiThread{
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage)
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage)

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setSixMonthDrivingDistance(){
        tv_driving_info1.text = "6개월 평균 고속 주행"
        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
        tv_driving_info3.text = "6개월 내 차의\n고속 주행 비율이에요"


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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


                    runOnUiThread{
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage)
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage)

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setYearDrivingDistance(){
        tv_driving_info1.text = "1년 평균 고속 주행"
        tv_driving_info2.text = "내 차는 고속 주행\n비율이 높을수록 좋아요"
        tv_driving_info3.text = "1년 내 차의\n고속 주행 비율이에요"

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@HighSpeedDrivingActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
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


                    runOnUiThread{
                        tv_total_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_diff_percent.text = "+" + String.format(Locale.KOREAN, "%.1f", drivingDistance.diffAverage.highSpeedDrivingDistancePercentage) + "% 증가"
                        tv_high_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.highSpeedDrivingDistancePercentage)
                        tv_low_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.lowSpeedDrivingDistancePercentage)
                        tv_etc_speed_percent.text = String.format(Locale.KOREAN, "%.1f", drivingDistance.average.etcSpeedDrivingDistancePercentage)

                        setHighSpeedDrivingChartWidthByPercent(drivingDistance.average.highSpeedDrivingDistancePercentage.toFloat()/100)
                        setLowSpeedDrivingChartWidthByPercent(drivingDistance.average.lowSpeedDrivingDistancePercentage.toFloat()/100)
                        setExtraSpeedDrivingChartWidthByPercent(drivingDistance.average.etcSpeedDrivingDistancePercentage.toFloat()/100)
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }


}