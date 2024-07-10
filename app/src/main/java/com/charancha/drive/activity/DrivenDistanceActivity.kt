package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driven_distance)

        init()
        setResources()
        setBarChart()
        setupLineChart()
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_distance = findViewById(R.id.layout_barchart_distance)
        layout_linechart_distance = findViewById(R.id.layout_linechart_distance)
    }

    private fun setBarChart() {
        val entries = listOf(
            BarEntry(0f, 0f),
            BarEntry(1f, 6f),
            BarEntry(2f, 10f),
            BarEntry(3f, 4f),
            BarEntry(4f, 8f),
            BarEntry(5f, 6f),
            BarEntry(6f, 2f),
            BarEntry(7f, 7f),
            BarEntry(8f, 5f),
            BarEntry(9f, 9f),
            BarEntry(10f, 3f),
            BarEntry(11f, 4f),
            BarEntry(12f, 5f),
            BarEntry(13f, 6f),
            BarEntry(14f, 2f),
            BarEntry(15f, 7f),
            BarEntry(16f, 5f),
            BarEntry(17f, 9f),
            BarEntry(18f, 3f),
            BarEntry(19f, 4f),
            BarEntry(20f, 5f),
            BarEntry(21f, 9f),
            BarEntry(22f, 1f),
            BarEntry(23f,2f),
            BarEntry(24f,5f),
            BarEntry(25f,0f),
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
        layout_barchart_distance.setExtraOffsets(0f, 0f, 0f, 0f)

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
        xAxis.granularity = 1.0f // only intervals of 1 unit
        xAxis.axisMinimum = -1f
        xAxis.axisMaximum = 25f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    0 -> "오전 12시"
                    8 -> "오전 6시"
                    16 -> "오후 12시"
                    24-> "오후 6시"
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

    private fun setupLineChart() {
        // 데이터 준비
        val entries = listOf(
            BarEntry(0f, 1f),
            BarEntry(1f, 2f),
            BarEntry(2f, 3f),
            BarEntry(3f, 4f),
            BarEntry(4f, 5f),
            BarEntry(5f, 6f),
            BarEntry(6f, 8f),
            BarEntry(7f, 10f),
            BarEntry(8f, 12f),
            BarEntry(9f, 14f),
            BarEntry(10f, 16f),
            BarEntry(11f, 18f),
            BarEntry(12f, 20f),
            BarEntry(13f, 23f),
            BarEntry(14f, 26f),
            BarEntry(15f, 29f),
            BarEntry(16f, 32f),
            BarEntry(17f, 35f),
            BarEntry(18f, 38f),
            BarEntry(19f, 41f),
            BarEntry(20f, 44f),
            BarEntry(21f, 47f),
            BarEntry(22f, 50f),
            BarEntry(23f,53f),
            BarEntry(24f,56f),
            BarEntry(25f,59f)

        )

        // 데이터셋 생성 및 설정
        val dataSet = LineDataSet(entries, "Label") // 데이터셋 생성
        dataSet.color = Color.BLACK // 선 색상 설정
        dataSet.setDrawValues(false) // 값 표시 여부 설정
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 곡선 형태로 설정
        dataSet.setDrawFilled(true)  // 선 안쪽을 색으로 채우도록 설정
        dataSet.fillColor = getColor(R.color.line_inner_start_color)  // 채우기 색상 설정

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

        // Customizing x-axis labels
        val xAxis = layout_linechart_distance.xAxis
        xAxis.granularity = 1.0f // only intervals of 1 unit
        xAxis.axisMinimum = -1f
        xAxis.axisMaximum = 25f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // X축의 그리드 라인 제거
        xAxis.textColor = getColor(R.color.gray_600)

        // Customizing x-axis labels
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    0 -> "오전 12시"
                    8 -> "오전 6시"
                    16 -> "오후 12시"
                    24-> "오후 6시"
                    else -> "" // 나머지 레이블은 비워둠
                }
            }
        }

        // Y축 레이블 및 선 제거
        val leftAxis = layout_linechart_distance.axisLeft
        leftAxis.setDrawGridLines(true) // 그리드 라인 표시
        leftAxis.setDrawAxisLine(false) // 축 라인 제거
        leftAxis.setDrawLabels(false) // Y축 레이블 제거
        leftAxis.setLabelCount(6, true) // 가로 라인의 수를 5로 설정 (강제)
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
                Log.d("testsetsetest","testesestest :: " + value)
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
    }
}