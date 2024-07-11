package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

    lateinit var btn_back:ImageView
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView


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

        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener { finish() }

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)

        btn_recent_drive.isSelected = true

        btn_recent_drive.setOnClickListener {
            btn_recent_drive.isSelected = true
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false

        }

        btn_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = true
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = false
        }

        btn_six_month_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = true
            btn_year_drive.isSelected = false
        }

        btn_year_drive.setOnClickListener {
            btn_recent_drive.isSelected = false
            btn_month_drive.isSelected = false
            btn_six_month_drive.isSelected = false
            btn_year_drive.isSelected = true
        }

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