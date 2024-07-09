package com.charancha.drive.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import com.charancha.drive.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter

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
    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)

        layout_barchart_distance = findViewById(R.id.layout_barchart_distance)
        layout_linechart_distance = findViewById(R.id.layout_linechart_distance)
    }
    
    private fun setBarChart(){
        val entries = listOf(
            BarEntry(1f, 4f),
            BarEntry(2f, 8f),
            BarEntry(3f, 6f),
            BarEntry(4f, 2f),
            BarEntry(5f, 7f),
            BarEntry(6f, 5f),
            BarEntry(7f, 9f),
            BarEntry(8f, 3f),
            BarEntry(9f, 4f),
            BarEntry(10f, 8f)
        )

        val dataSet = BarDataSet(entries, "Sample Data")
        dataSet.color = getColor(R.color.gray_200)
        dataSet.valueTextColor = getColor(R.color.gray_50)
        dataSet.valueTextSize = 16f

        val barData = BarData(dataSet)
        layout_barchart_distance.data = barData

        layout_barchart_distance.setFitBars(true) // make the x-axis fit exactly all bars
        layout_barchart_distance.description.isEnabled = false
        layout_barchart_distance.animateY(1000)
        layout_barchart_distance.legend.isEnabled = false

        // Customizing x-axis labels
        val xAxis = layout_barchart_distance.xAxis
        xAxis.granularity =1f // only intervals of 1 unit

        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 10f


        layout_barchart_distance.invalidate() // refresh
    }

    private fun setResources(){
        btn_back.setOnClickListener { finish() }
    }
}