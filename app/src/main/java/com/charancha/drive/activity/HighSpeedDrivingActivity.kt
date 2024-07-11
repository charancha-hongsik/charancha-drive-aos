package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highspeed_driving)

        init()

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

        setHighSpeedDrivingChartWidthByPercent(0.341f)
        setLowSpeedDrivingChartWidthByPercent(0.618f)
        setExtraSpeedDrivingChartWidthByPercent(0.41f)



    }

    /**
     * 0.0 ~ 1
     */
    fun setHighSpeedDrivingChartWidthByPercent(percent:Float){
        layout_high_speed_background.post {
            val backgroundWidth = layout_high_speed_background.width

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

    /**
     * 0.0 ~ 1
     */
    fun setLowSpeedDrivingChartWidthByPercent(percent:Float){
        layout_low_speed_background.post {
            val backgroundWidth = layout_low_speed_background.width

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
}