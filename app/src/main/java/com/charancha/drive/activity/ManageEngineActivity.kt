package com.charancha.drive.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.response.GetDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GetManageScoreResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ManageEngineActivity:BaseRefreshActivity() {
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
    lateinit var btn_recent_drive:TextView
    lateinit var btn_month_drive:TextView
    lateinit var btn_six_month_drive:TextView
    lateinit var btn_year_drive:TextView
    lateinit var btn_high_speed_driving:ConstraintLayout
    lateinit var btn_optimal_driving:TextView
    lateinit var btn_normal_speed_driving:TextView

    lateinit var layout_no_score:ConstraintLayout
    lateinit var tv_no_score:TextView
    lateinit var tv_no_score1:TextView
    lateinit var iv_no_score:ImageView

    lateinit var tv_distance:TextView
    lateinit var tv_speed_percent:TextView
    lateinit var tv_optimal_driving_percent:TextView

    lateinit var tv_normal_speed_driving_percent:TextView

    lateinit var tv_optimal_driving_contents:TextView
    lateinit var tv_normal_speed_driving_contents:TextView

    lateinit var iv_tooltip_perone_average:ImageView
    lateinit var iv_tooltip_high_speed:ImageView
    lateinit var iv_tooltip_optimal_driving:ImageView
    lateinit var iv_tooltip_const_driving:ImageView
    lateinit var tv_distance_unit:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_engine)

        init()
        setRecentAllForEngine()
    }

    fun init(){
        setResources()
    }

    fun setResources(){
        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener {
            finish()
        }

        iv_tooltip_perone_average = findViewById(R.id.iv_tooltip_perone_average)
        iv_tooltip_perone_average.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@ManageEngineActivity, "1회 평균 주행거리란?","차량이 한 번 주행할 때마다 이동한 거리의 평균값이에요. 높을수록 좋아요!")
            }
        })




        iv_tooltip_high_speed = findViewById(R.id.iv_tooltip_high_speed)
        iv_tooltip_high_speed.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@ManageEngineActivity, "고속 주행이란?","80km/h 이상 150km/h 미만 사이의 속력으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })


        iv_tooltip_optimal_driving = findViewById(R.id.iv_tooltip_optimal_driving)
        iv_tooltip_optimal_driving.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@ManageEngineActivity, "최적 주행이란?","급출발, 급가속, 급정지, 급감속을 하지 않고 안정적으로 주행한 거리에요. 높을수록 좋아요!")
            }
        })

        iv_tooltip_const_driving = findViewById(R.id.iv_tooltip_const_driving)
        iv_tooltip_const_driving.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                showTooltipForEach(this@ManageEngineActivity, "항속 주행이란?","61km/h 이상 141km/h 미만으로 3분 이상 일정한 속도로 주행한 거리에요(속도 변동 5km/h 이내). 높을수록 좋아요!")
            }
        })

        tv_optimal_driving_contents = findViewById(R.id.tv_optimal_driving_contents)

        tv_optimal_driving_percent = findViewById(R.id.tv_optimal_driving_percent)
        tv_distance = findViewById(R.id.tv_distance)
        tv_speed_percent = findViewById(R.id.tv_speed_percent)

        tv_normal_speed_driving_percent = findViewById(R.id.tv_normal_speed_driving_percent)
        tv_normal_speed_driving_contents = findViewById(R.id.tv_normal_speed_driving_contents)

        layout_no_score = findViewById(R.id.layout_no_score)
        tv_no_score = findViewById(R.id.tv_no_score)
        tv_no_score1 = findViewById(R.id.tv_no_score1)
        iv_no_score = findViewById(R.id.iv_no_score)

        view_normal_speed_driving_chart = findViewById(R.id.view_normal_speed_driving_chart)
        view_optimal_driving_chart = findViewById(R.id.view_optimal_driving_chart)

        view_normal_speed_driving_chart_background = findViewById(R.id.view_normal_speed_driving_chart_background)
        view_optimal_driving_chart_background = findViewById(R.id.view_optimal_driving_chart_background)

        tv_optimal_driving_percent1 = findViewById(R.id.tv_optimal_driving_percent1)
        tv_optimal_driving_percent2 = findViewById(R.id.tv_optimal_driving_percent2)
        tv_normal_speed_driving_percent1 = findViewById(R.id.tv_normal_speed_driving_percent1)
        tv_normal_speed_driving_percent2 = findViewById(R.id.tv_normal_speed_driving_percent2)

        btn_recent_drive = findViewById(R.id.btn_recent_drive)
        btn_month_drive = findViewById(R.id.btn_month_drive)
        btn_six_month_drive = findViewById(R.id.btn_six_month_drive)
        btn_year_drive = findViewById(R.id.btn_year_drive)
        tv_distance_unit = findViewById(R.id.tv_distance_unit)
        tv_distance_unit.text = distance_unit

        btn_high_speed_driving = findViewById(R.id.btn_high_speed_driving)
        btn_high_speed_driving.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@ManageEngineActivity, HighSpeedDrivingActivity::class.java))
            }

        })

        btn_average_distance = findViewById(R.id.btn_average_distance)
        btn_average_distance.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@ManageEngineActivity, AverageDrivenDistanceActivity::class.java))
            }

        })


        btn_optimal_driving = findViewById(R.id.btn_optimal_driving)
        btn_optimal_driving.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@ManageEngineActivity, BestDrivingActivity::class.java))
            }

        })

        btn_normal_speed_driving = findViewById(R.id.btn_normal_speed_driving)
        btn_normal_speed_driving.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@ManageEngineActivity, ConstantSpeedDrivingActivity::class.java))
            }

        })


        btn_recent_drive.isSelected = true

        btn_recent_drive.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent_drive.isSelected = true
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false

                setRecentAllForEngine()
            }

        })

        btn_month_drive.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = true
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = false

                setAllForEngine(29)
            }
        })

        btn_six_month_drive.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = true
                btn_year_drive.isSelected = false

                setAllForEngine(150)
            }

        })

        btn_year_drive.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent_drive.isSelected = false
                btn_month_drive.isSelected = false
                btn_six_month_drive.isSelected = false
                btn_year_drive.isSelected = true

                setAllForEngine(334)
            }
        })

        setNormalSpeedDrivingChartWidthByPercent(0f)
        setOptimalDrivingChartWidthByPercent(0f)


    }

    /**
     * 0.0 ~ 1
     */
    fun setNormalSpeedDrivingChartWidthByPercent(percent:Float){
        view_normal_speed_driving_chart_background.post {
            if(percent == 0f){
                val layoutParams = view_normal_speed_driving_chart.layoutParams
                layoutParams.width = 1
                view_normal_speed_driving_chart.layoutParams = layoutParams

                view_normal_speed_driving_chart.visibility = INVISIBLE

            }else{
                val backgroundWidth = view_normal_speed_driving_chart_background.width

                // Calculate 70% of the background view's width
                val chartWidth = (backgroundWidth * percent).toInt()


                // Apply the calculated width to view_normal_speed_driving_chart
                val layoutParams = view_normal_speed_driving_chart.layoutParams
                layoutParams.width = chartWidth
                view_normal_speed_driving_chart.layoutParams = layoutParams

                view_normal_speed_driving_chart.visibility = VISIBLE

            }
        }
    }

    /**
     * 0.0 ~ 1
     */
    fun setOptimalDrivingChartWidthByPercent(percent:Float){
        view_optimal_driving_chart_background.post {
            if(percent == 0f){
                val layoutParams = view_optimal_driving_chart.layoutParams
                layoutParams.width = 1
                view_optimal_driving_chart.layoutParams = layoutParams

                view_optimal_driving_chart.visibility = INVISIBLE
            }else{
                val backgroundWidth = view_optimal_driving_chart_background.width

                // Calculate 70% of the background view's width
                val chartWidth = (backgroundWidth * percent).toInt()

                // Apply the calculated width to view_normal_speed_driving_chart
                val layoutParams = view_optimal_driving_chart.layoutParams
                layoutParams.width = chartWidth
                view_optimal_driving_chart.layoutParams = layoutParams
            }
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


    fun setRecentAllForEngine(){
        apiService().getRecentManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@ManageEngineActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@ManageEngineActivity, PreferenceUtil.USER_CARID, "")!!
        ).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                    if(getManageScoreResponse.isRecent){
                        if(getManageScoreResponse.total.totalEngineScore != 0.0){
                            tv_no_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                            if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
                                layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray950)

                                tv_no_score1.text = "점수 변동이 없어요"
                                iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                            }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
                                layout_no_score.background = resources.getDrawable(R.drawable.radius8_pri500)

                                tv_no_score1.text = "굉장해요. 지난 주행보다 +" +  transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점을 얻었어요!"
                                iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                            }else if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
                                layout_no_score.background = resources.getDrawable(R.drawable.radius8_sec)

                                tv_no_score1.text = "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
                                iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                            }

                        }else{
                            layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray800)

                            tv_no_score.text = "0"
                            tv_no_score1.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                        }
                    }else{
                        layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray800)

                        tv_no_score.text = "0"
                        tv_no_score1.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })

        apiService().getRecentDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@ManageEngineActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getDrivingStatisticsResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(getDrivingStatisticsResponse.total.totalDistance != 0.0){
                        tv_optimal_driving_contents.text = "최적 주행이 높을수록 좋아요!"
                        tv_normal_speed_driving_contents.text = "항속 주행이 높을수록 좋아요!"
                        tv_distance.text = transferDistance(getDrivingStatisticsResponse.perOneAverage.totalDistance)
                        tv_speed_percent.text = transferNumWithRounds(getDrivingStatisticsResponse.average.highSpeedDrivingDistancePercentage).toString()

                        tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)
                        tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)
                        tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)

                        tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)
                        tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)
                        tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)


                        setOptimalDrivingChartWidthByPercent((getDrivingStatisticsResponse.average.optimalDrivingPercentage/100).toFloat())
                        setNormalSpeedDrivingChartWidthByPercent((getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage/100).toFloat())
                        setOptimalDrivingPercentTextView()
                        setNormalDrivingPercentTextView()

                    }else{
                        tv_optimal_driving_contents.text = "아직 데이터가 없어요."
                        tv_normal_speed_driving_contents.text = "아직 데이터가 없어요."


                        tv_distance.text = transferDistance(0.0)
                        tv_speed_percent.text = transferNumWithRounds(0.0).toString()

                        tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                        tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                        setOptimalDrivingChartWidthByPercent(0f)
                        setNormalSpeedDrivingChartWidthByPercent(0f)
                    }



                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_optimal_driving_contents.text = "아직 데이터가 없어요."
                tv_normal_speed_driving_contents.text = "아직 데이터가 없어요."

                tv_distance.text = transferDistance(0.0)
                tv_speed_percent.text = transferNumWithRounds(0.0).toString()

                tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)
            }

        })
    }

    fun setAllForEngine(scope:Long){
        apiService().getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@ManageEngineActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@ManageEngineActivity, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                    if(getManageScoreResponse.total.totalEngineScore != 0.0){
                        tv_no_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                        if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
                            layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray950)

                            tv_no_score1.text = "점수 변동이 없어요"
                            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                        }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
                            layout_no_score.background = resources.getDrawable(R.drawable.radius8_pri500)

                            tv_no_score1.text = "굉장해요. 지난 주행보다 +" +  transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점을 얻었어요!"
                            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                        }else if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
                            layout_no_score.background = resources.getDrawable(R.drawable.radius8_sec)

                            tv_no_score1.text = "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
                            iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                        }

                    }else{
                        layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray800)

                        tv_no_score.text = "0"
                        tv_no_score1.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                layout_no_score.background = resources.getDrawable(R.drawable.radius8_gray800)

                tv_no_score.text = "0"
                tv_no_score1.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                iv_no_score.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
            }
        })
        var timUnit = "day"

        if(scope != 29L){
            timUnit = "month"
        }


        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@ManageEngineActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first,
            "startTime",
            timUnit).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDrivingStatisticsResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(getDrivingStatisticsResponse.total.totalDistance != 0.0){
                        tv_optimal_driving_contents.text = "최적 주행이 높을수록 좋아요!"
                        tv_normal_speed_driving_contents.text = "항속 주행이 높을수록 좋아요!"

                        tv_speed_percent.text = transferNumWithRounds(getDrivingStatisticsResponse.average.highSpeedDrivingDistancePercentage).toString()/**/


                        tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)
                        tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)
                        tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.optimalDrivingPercentage)


                        tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)
                        tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)
                        tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage)


                        setOptimalDrivingChartWidthByPercent((getDrivingStatisticsResponse.average.optimalDrivingPercentage/100).toFloat())
                        setNormalSpeedDrivingChartWidthByPercent((getDrivingStatisticsResponse.average.constantSpeedDrivingDistancePercentage/100).toFloat())
                        setOptimalDrivingPercentTextView()
                        setNormalDrivingPercentTextView()
                    }else{
                        tv_optimal_driving_contents.text = "아직 데이터가 없어요."
                        tv_normal_speed_driving_contents.text = "아직 데이터가 없어요."


                        tv_distance.text = transferDistance(0.0)
                        tv_speed_percent.text = transferNumWithRounds(0.0).toString()

                        tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                        tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                        tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                        setOptimalDrivingChartWidthByPercent(0f)
                        setNormalSpeedDrivingChartWidthByPercent(0f)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_optimal_driving_contents.text = "아직 데이터가 없어요."
                tv_normal_speed_driving_contents.text = "아직 데이터가 없어요."


                tv_distance.text = transferDistance(0.0)
                tv_speed_percent.text = transferNumWithRounds(0.0).toString()

                tv_optimal_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_optimal_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_optimal_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                tv_normal_speed_driving_percent.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_normal_speed_driving_percent1.text = String.format(Locale.KOREAN, "%.0f", 0.0)
                tv_normal_speed_driving_percent2.text = String.format(Locale.KOREAN, "%.0f", 0.0)

                setOptimalDrivingChartWidthByPercent(0f)
                setNormalSpeedDrivingChartWidthByPercent(0f)
            }

        })

        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@ManageEngineActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first,
            "startTime",
            "").enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDrivingStatisticsResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )

                    if(getDrivingStatisticsResponse.total.totalDistance != 0.0){
                        tv_distance.text = transferDistance(getDrivingStatisticsResponse.perOneAverage.totalDistance)
                    }else{
                        tv_optimal_driving_contents.text = "아직 데이터가 없어요."
                        tv_normal_speed_driving_contents.text = "아직 데이터가 없어요."


                        tv_distance.text = transferDistance(0.0)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                tv_distance.text = transferDistance(0.0)
            }

        })

    }

}