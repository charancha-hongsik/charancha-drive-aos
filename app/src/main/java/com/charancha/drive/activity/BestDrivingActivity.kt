package com.charancha.drive.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class BestDrivingActivity:BaseActivity() {
    lateinit var layout_extra_speed_percent: View
    lateinit var layout_extra_speed_extra:View
    lateinit var layout_extra_speed_background:ConstraintLayout

    lateinit var btn_back:ImageView
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView

    lateinit var layout_barchart_best_driving:BarChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_best_driving)

        init()

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

        btn_recent_drive.isSelected = true

        btn_recent_drive.setOnClickListener {
            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            setRecentBarChart()

        }

        btn_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

            setMonthBarChart()
        }

        btn_six_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false

            setSixMonthBarChart()
        }

        btn_year_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true

            setYearBarChart()
        }

        setRecentBarChart()

        setExtraSpeedDrivingChartWidthByPercent(0.81f)
    }


    /**
     * 0.0 ~ 1
     */
    fun setExtraSpeedDrivingChartWidthByPercent(percent:Float){
        layout_extra_speed_background.post {
            val backgroundWidth = layout_extra_speed_background.width

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

        layout_barchart_best_driving.invalidate() // refresh
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
                    1 -> "6월 17일"
                    10 -> "6월 24일"
                    18 -> "7월 1일"
                    26-> "7월 8일"
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

        layout_barchart_best_driving.invalidate() // refresh
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

        layout_barchart_best_driving.invalidate() // refresh
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
                    -1 -> "1월"
                    7 -> "5월"
                    13 -> "8월"
                    21-> "12월"
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

        layout_barchart_best_driving.invalidate() // refresh
    }


}