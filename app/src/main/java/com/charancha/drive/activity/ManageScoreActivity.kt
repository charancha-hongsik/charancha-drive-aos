package com.charancha.drive.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.R
import kotlin.math.roundToInt

class ManageScoreActivity:BaseActivity() {
    lateinit var btn_back: ImageView
    lateinit var view_normal_speed_driving_chart:View
    lateinit var view_optimal_driving_chart:View
    lateinit var view_optimal_driving_chart_background:View
    lateinit var view_normal_speed_driving_chart_background:View
    lateinit var tv_optimal_driving_percent1: TextView
    lateinit var tv_optimal_driving_percent2: TextView
    lateinit var tv_normal_speed_driving_percent1: TextView
    lateinit var tv_normal_speed_driving_percent2: TextView
    lateinit var btn_average_distance: ConstraintLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_managescore)

        init()

    }

    fun init(){
        setResources()
    }

    fun setResources(){
        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener {
            finish()
        }

        view_normal_speed_driving_chart = findViewById(R.id.view_normal_speed_driving_chart)
        view_optimal_driving_chart = findViewById(R.id.view_optimal_driving_chart)

        view_normal_speed_driving_chart_background = findViewById(R.id.view_normal_speed_driving_chart_background)
        view_optimal_driving_chart_background = findViewById(R.id.view_optimal_driving_chart_background)

        tv_optimal_driving_percent1 = findViewById(R.id.tv_optimal_driving_percent1)
        tv_optimal_driving_percent2 = findViewById(R.id.tv_optimal_driving_percent2)
        tv_normal_speed_driving_percent1 = findViewById(R.id.tv_normal_speed_driving_percent1)
        tv_normal_speed_driving_percent2 = findViewById(R.id.tv_normal_speed_driving_percent2)

        btn_average_distance = findViewById(R.id.btn_average_distance)
        btn_average_distance.setOnClickListener {

        }


        setOptimalDrivingChartWidthByPercent(1f)
        setNormalSpeedDrivingChartWidthByPercent(0.22f)
        setOptimalDrivingPercentTextView()
        setNormalDrivingPercentTextView()
    }

    /**
     * 0.0 ~ 1
     */
    fun setNormalSpeedDrivingChartWidthByPercent(percent:Float){
        view_normal_speed_driving_chart_background.post {
            val backgroundWidth = view_normal_speed_driving_chart_background.width

            // Calculate 70% of the background view's width
            val chartWidth = (backgroundWidth * percent).toInt()

            // Apply the calculated width to view_normal_speed_driving_chart
            val layoutParams = view_normal_speed_driving_chart.layoutParams
            layoutParams.width = chartWidth
            view_normal_speed_driving_chart.layoutParams = layoutParams
        }
    }

    /**
     * 0.0 ~ 1
     */
    fun setOptimalDrivingChartWidthByPercent(percent:Float){
        view_optimal_driving_chart_background.post {
            val backgroundWidth = view_optimal_driving_chart_background.width

            // Calculate 70% of the background view's width
            val chartWidth = (backgroundWidth * percent).toInt()

            // Apply the calculated width to view_normal_speed_driving_chart
            val layoutParams = view_optimal_driving_chart.layoutParams
            layoutParams.width = chartWidth
            view_optimal_driving_chart.layoutParams = layoutParams
        }
    }


    fun setOptimalDrivingPercentTextView(){
        view_optimal_driving_chart.viewTreeObserver.addOnGlobalLayoutListener {
            tv_optimal_driving_percent1.viewTreeObserver.addOnGlobalLayoutListener {
                val chartWidth = view_optimal_driving_chart.width
                val percentWidth = tv_optimal_driving_percent1.width

                val widthDifference = chartWidth - percentWidth

                if (widthDifference > 11) {
                    tv_optimal_driving_percent1.visibility = GONE
                    tv_optimal_driving_percent2.visibility = VISIBLE
                }else{
                    tv_optimal_driving_percent1.visibility = VISIBLE
                    tv_optimal_driving_percent2.visibility = GONE
                }
            }
        }
    }

    fun setNormalDrivingPercentTextView(){

        view_normal_speed_driving_chart.viewTreeObserver.addOnGlobalLayoutListener {
            tv_normal_speed_driving_percent1.viewTreeObserver.addOnGlobalLayoutListener {
                val chartWidth = pxToDp(view_normal_speed_driving_chart.width)
                val percentWidth = pxToDp(tv_normal_speed_driving_percent1.width)

                val widthDifference = chartWidth - percentWidth

                if (widthDifference > 11) {
                    tv_normal_speed_driving_percent1.visibility = GONE
                    tv_normal_speed_driving_percent2.visibility = VISIBLE
                }else{
                    tv_normal_speed_driving_percent1.visibility = VISIBLE
                    tv_normal_speed_driving_percent2.visibility = GONE
                }
            }
        }

    }

    fun pxToDp(px: Int): Float {
        val density = resources.displayMetrics.density

        return px / density
    }
}