package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.charancha.drive.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class DrivenDistanceActivity:BaseActivity() {
    lateinit var btn_back:ImageView
    lateinit var layout_barchart_distance:BarChart
    lateinit var layout_linechart_distance:LineChart
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_distance)

        init()
        setResources()
        setRecentBarChart()
        setRecentLineChart()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_distance = findViewById(R.id.layout_barchart_distance)
        layout_linechart_distance = findViewById(R.id.layout_linechart_distance)

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

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
            setRecentBarChart()
            setRecentLineChart()

            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false
        }

        btn_month_drive.setOnClickListener {
            setMonthBarChart()
            setMonthLineChart()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

        }

        btn_six_month_drive.setOnClickListener {
            setSixMonthBarChart()
            setSixMonthLineChart()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false
        }

        btn_year_drive.setOnClickListener {
            setYearBarChart()
            setYearLineChart()

            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true
        }
    }
}