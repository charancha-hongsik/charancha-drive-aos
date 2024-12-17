package com.milelog.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.room.entity.MyCarsEntity
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.MyScoreViewModel
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.CarInfoInquiryByCarIdState
import com.milelog.viewmodel.state.GetDrivingStatisticsState
import com.milelog.viewmodel.state.GetManageScoreState
import com.milelog.viewmodel.state.MyCarInfoState
import com.milelog.viewmodel.state.NotSavedDataState
import java.text.SimpleDateFormat
import java.util.*


/**
 * 홈화면에 필요한 데이터
 * 1. 평균 주행 거리 (지난 30일간)
 * 2. 평균 주행 시간 (지난 30일간)
 * 3. 최근 관리 점수
 * 4. 평균 점수
 * 5. 최근 주행 총점
 * 6.
 */
class MyScoreActivity : BaseRefreshActivity() {
    private val myScoreViewModel: MyScoreViewModel by viewModels()

    lateinit var btnHistory: ImageView

    lateinit var chart: PieChart
    lateinit var layout_engine: ConstraintLayout
    lateinit var layout_average_distance:ConstraintLayout
    lateinit var layout_average_time:ConstraintLayout
    lateinit var button_average_score_overlay:Button
    lateinit var layout_recent_manage_score:ConstraintLayout
    lateinit var tv_car_name:TextView
    lateinit var tv_car_no:TextView
    lateinit var tv_average_score:TextView
    lateinit var tv_increase:TextView
    lateinit var tv_average_distance_contents:TextView
    lateinit var tv_average_time_contents:TextView

    lateinit var view_diff_distance_background:ConstraintLayout
    lateinit var view_no_diff_distance: View
    lateinit var view_there_is_diff_distance: LinearLayout
    lateinit var iv_there_is_diff_distance: ImageView
    lateinit var tv_there_is_diff_distance:TextView

    lateinit var view_diff_time_background:ConstraintLayout
    lateinit var view_no_diff_time: View
    lateinit var view_there_is_diff_time: LinearLayout
    lateinit var iv_there_is_diff_time: ImageView
    lateinit var tv_there_is_diff_time:TextView
    lateinit var tv_recent_score:TextView
    lateinit var tv_recent_score2:TextView
    lateinit var tv_recent_info_text:TextView
    lateinit var iv_recent_info:ImageView
    lateinit var btn_recent:TextView
    lateinit var btn_one_month:TextView
    lateinit var btn_six_month:TextView
    lateinit var btn_one_year:TextView
    lateinit var tv_engine_score:TextView
    lateinit var iv_home_banner:ImageView
    lateinit var tv_recent_driving_score:TextView
    lateinit var tv_subtitle2:TextView
    lateinit var tv_recent_date:TextView

    lateinit var tv_guide_subtitle:TextView

    lateinit var userCarId:String
    lateinit var btn_back:View


    companion object {
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_score)

        init()
        myScoreViewModel.init(applicationContext)
        setObserver()

        myScoreViewModel.getAccount()
        myScoreViewModel.getMyCarInfo()
    }

    private fun init(){
        userCarId = intent.getStringExtra("userCarId")?:PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!
        setPieChart(0.0f)

        setLineChartForBrakes(findViewById(R.id.chart_line_brakes))
        setLineChartForEngine(findViewById(R.id.chart_line_engine))
        setLineChartForTire(findViewById(R.id.chart_line_tire))

        setBtn()
    }

    fun updateMyCarList(
        myCarsListOnServer: MutableList<MyCarsEntity>,
        myCarsListOnDevice: MutableList<MyCarsEntity>
    ): MutableList<MyCarsEntity> {
        // 1. 유지할 리스트: 서버에 있는 차량만 남기고 type과 name 동기화
        val retainedCars = myCarsListOnDevice.mapNotNull { deviceCar ->
            myCarsListOnServer.find { serverCar -> serverCar.id == deviceCar.id }?.let { serverCar ->
                deviceCar.copy(name = serverCar.name, type = serverCar.type, isActive = serverCar.isActive) // type과 name을 서버의 값으로 동기화
            }
        }.toMutableList()

        // 2. 추가할 차량: 서버에 있는데 장치에 없는 차량 추가
        val newCarsToAdd = myCarsListOnServer.filterNot { serverCar ->
            myCarsListOnDevice.any { deviceCar -> deviceCar.id == serverCar.id }
        }

        // 3. 새 차량을 유지된 차량 리스트에 추가
        retainedCars.addAll(newCarsToAdd)

        // 업데이트된 리스트 반환
        return retainedCars
    }

    private fun setObserver(){
        myScoreViewModel.accountResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is AccountState.Loading -> {

                }
                is AccountState.Success -> {
                    val getAccountResponse = state.data
                    PreferenceUtil.putPref(this@MyScoreActivity, PreferenceUtil.USER_ID, getAccountResponse.id)
                }
                is AccountState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is AccountState.Empty -> {

                }

                else -> {

                }
            }
        })

        myScoreViewModel.notSavedDataStateResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is NotSavedDataState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
            }
        })

        myScoreViewModel.myCarInfoResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is MyCarInfoState.Loading -> {

                }
                is MyCarInfoState.Success -> {
                    val getMyCarInfoResponses = state.data

                    val myCarsListOnServer: MutableList<MyCarsEntity> = mutableListOf()
                    val myCarsListOnDevice:MutableList<MyCarsEntity> = mutableListOf()

                    PreferenceUtil.getPref(this@MyScoreActivity, PreferenceUtil.MY_CAR_ENTITIES,"")?.let{
                        if(it != "") {
                            val type = object : TypeToken<MutableList<MyCarsEntity>>() {}.type
                            myCarsListOnDevice.addAll(GsonBuilder().serializeNulls().create().fromJson(it, type))
                        }
                    }

                    if(getMyCarInfoResponses.items.size > 0){
                        myScoreViewModel.getCarInfoinquiryByCarId(userCarId)

                        for(car in getMyCarInfoResponses.items){
                            myCarsListOnServer.add(MyCarsEntity(car.id, name = car.makerNm + " " + car.modelNm, fullName = car.carName, car.licensePlateNumber, null,null, type = car.type))
                        }

                        PreferenceUtil.putPref(this@MyScoreActivity, PreferenceUtil.MY_CAR_ENTITIES, GsonBuilder().serializeNulls().create().toJson(updateMyCarList(myCarsListOnServer, myCarsListOnDevice)))

                    }else{
                        startActivity(Intent(this@MyScoreActivity, SplashActivity::class.java))
                        finish()
                    }
                }
                is MyCarInfoState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is MyCarInfoState.Empty -> {

                }
            }
        })

        myScoreViewModel.carInfoInquiryByCarId.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is CarInfoInquiryByCarIdState.Loading -> {

                }
                is CarInfoInquiryByCarIdState.Success -> {
                    val getMyCarInfoResponse = state.data
                    tv_car_name.setText(getMyCarInfoResponse.makerNm + " " + getMyCarInfoResponse.modelNm)
                    tv_car_no.setText(getMyCarInfoResponse.licensePlateNumber)

                    myScoreViewModel.getManageScoreForAMonth(userCarId)
                    myScoreViewModel.getDrivingDistanceForAMonth(userCarId)
                    myScoreViewModel.setRecentManageScoreForSummary(userCarId)
                }
                is CarInfoInquiryByCarIdState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is CarInfoInquiryByCarIdState.Empty -> {

                }
            }
        })

        myScoreViewModel.managerScoreResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetManageScoreState.Loading -> {

                }
                is GetManageScoreState.Success -> {
                    val getManageScoreResponse = state.data
                    tv_average_score.text =
                        transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                    if (getManageScoreResponse.diffAverage.totalEngineScore == 0.0) {
                        tv_increase.text = "변동 없음"
                        tv_increase.setTextColor(resources.getColor(R.color.gray_500))
                    } else if (getManageScoreResponse.diffAverage.totalEngineScore > 0.0) {
                        tv_increase.text =
                            "+" + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 증가"
                        tv_increase.setTextColor(resources.getColor(R.color.pri_800))
                    } else if (getManageScoreResponse.diffAverage.totalEngineScore < 0.0) {
                        tv_increase.text =
                            transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore).toString() + "점 하락"
                        tv_increase.setTextColor(resources.getColor(R.color.sec_500))
                    }

                    setPieChart((getManageScoreResponse.average.totalEngineScore / 10).toFloat())
                }
                is GetManageScoreState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetManageScoreState.Empty -> {
                    setPieChart(0.0f)
                }
            }
        })

        myScoreViewModel.drivingStatisticsResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetDrivingStatisticsState.Loading -> {

                }
                is GetDrivingStatisticsState.Success -> {
                    val getDrivingStatisticsResponse = state.data

                    tv_average_distance_contents.text =
                        transferDistanceWithUnit(getDrivingStatisticsResponse.average.totalDistance)
                    tv_average_time_contents.text =
                        transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.average.totalTime).first.toString() + "시간" + transferSecondsToHourAndMinutes(
                            getDrivingStatisticsResponse.average.totalTime
                        ).second.toString() + "분"

                    if (getDrivingStatisticsResponse.diffAverage.totalDistance == 0.0) {
                        view_diff_distance_background.background =
                            resources.getDrawable(R.drawable.radius999_gray100)
                        view_no_diff_distance.visibility = VISIBLE
                        view_there_is_diff_distance.visibility = GONE

                    } else if (getDrivingStatisticsResponse.diffAverage.totalDistance > 0.0) {
                        view_diff_distance_background.background =
                            resources.getDrawable(R.drawable.radius999_pri50)
                        view_no_diff_distance.visibility = GONE
                        view_there_is_diff_distance.visibility = VISIBLE
                        iv_there_is_diff_distance.setImageDrawable(resources.getDrawable(R.drawable.vector_pri))
                        tv_there_is_diff_distance.setText(
                            transferDistanceWithUnit(
                                getDrivingStatisticsResponse.diffAverage.totalDistance
                            )
                        )
                        tv_there_is_diff_distance.setTextColor(resources.getColor(R.color.pri_800))


                    } else if (getDrivingStatisticsResponse.diffAverage.totalDistance < 0.0) {
                        view_diff_distance_background.background =
                            resources.getDrawable(R.drawable.radius999_sec50)
                        view_no_diff_distance.visibility = GONE
                        view_there_is_diff_distance.visibility = VISIBLE
                        iv_there_is_diff_distance.setImageDrawable(resources.getDrawable(R.drawable.vector_sec))
                        tv_there_is_diff_distance.setText(
                            transferDistanceWithUnit(
                                getDrivingStatisticsResponse.diffAverage.totalDistance
                            )
                        )
                        tv_there_is_diff_distance.setTextColor(resources.getColor(R.color.sec_500))

                    }

                    if (getDrivingStatisticsResponse.diffAverage.totalTime == 0.0) {
                        view_diff_time_background.background =
                            resources.getDrawable(R.drawable.radius999_gray100)
                        view_no_diff_time.visibility = VISIBLE
                        view_there_is_diff_time.visibility = GONE


                    } else if (getDrivingStatisticsResponse.diffAverage.totalTime > 0.0) {
                        view_diff_time_background.background =
                            resources.getDrawable(R.drawable.radius999_pri50)
                        view_no_diff_time.visibility = GONE
                        view_there_is_diff_time.visibility = VISIBLE
                        iv_there_is_diff_time.setImageDrawable(resources.getDrawable(R.drawable.vector_pri))
                        tv_there_is_diff_time.setText(
                            transferSecondsToHourAndMinutes(
                                getDrivingStatisticsResponse.diffAverage.totalTime
                            ).first.toString() + "시간" + transferSecondsToHourAndMinutes(
                                getDrivingStatisticsResponse.diffAverage.totalTime
                            ).second.toString() + "분"
                        )
                        tv_there_is_diff_time.setTextColor(resources.getColor(R.color.pri_800))


                    } else if (getDrivingStatisticsResponse.diffAverage.totalTime < 0.0) {
                        view_diff_time_background.background =
                            resources.getDrawable(R.drawable.radius999_sec50)
                        view_no_diff_time.visibility = GONE
                        view_there_is_diff_time.visibility = VISIBLE
                        iv_there_is_diff_time.setImageDrawable(resources.getDrawable(R.drawable.vector_sec))
                        tv_there_is_diff_time.setText(
                            transferSecondsToHourAndMinutes(
                                getDrivingStatisticsResponse.diffAverage.totalTime
                            ).first.toString() + "시간" + transferSecondsToHourAndMinutes(
                                getDrivingStatisticsResponse.diffAverage.totalTime
                            ).second.toString() + "분"
                        )
                        tv_there_is_diff_time.setTextColor(resources.getColor(R.color.sec_500))


                    }
                }
                is GetDrivingStatisticsState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetDrivingStatisticsState.Empty -> {

                }
            }
        })

        myScoreViewModel.recentManageScoreResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetManageScoreState.Loading -> {

                }
                is GetManageScoreState.Success -> {
                    val getManageScoreResponse = state.data

                    if (getManageScoreResponse.isRecent) {
                        if (getManageScoreResponse.total.totalEngineScore != 0.0) {
                            tv_recent_date.visibility = VISIBLE
                            tv_recent_date.text = formatDate(getManageScoreResponse.recentCriteriaAt.split("T").first())

                            tv_recent_score.text =
                                transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                            tv_recent_score2.text =
                                transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                            tv_engine_score.text =
                                transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                            if (getManageScoreResponse.diffAverage.totalEngineScore == 0.0) {
                                tv_recent_info_text.text = "점수 변동이 없어요"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                            } else if (getManageScoreResponse.diffAverage.totalEngineScore > 0.0) {
                                tv_recent_info_text.text =
                                    "굉장해요. 지난 주행보다 +" + transferNumWithRounds(
                                        getManageScoreResponse.diffAverage.totalEngineScore
                                    ) + "점을 얻었어요!"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                            } else if (getManageScoreResponse.diffAverage.totalEngineScore < 0.0) {
                                tv_recent_info_text.text =
                                    "아쉬워요. 지난 주행보다 " + transferNumWithRounds(
                                        getManageScoreResponse.diffAverage.totalEngineScore
                                    ) + "점 하락했어요"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                            }

                        } else {
                            tv_recent_date.visibility = GONE
                            tv_recent_score2.text = "0"
                            tv_engine_score.text = "0"

                            tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                        }
                    } else {
                        tv_recent_date.visibility = GONE
                        tv_recent_score2.text = "0"
                        tv_engine_score.text = "0"

                        tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
                is GetManageScoreState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetManageScoreState.Empty -> {

                }
            }
        })

        myScoreViewModel.manageScoreForSummaryResult.observe(this@MyScoreActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetManageScoreState.Loading -> {

                }
                is GetManageScoreState.Success -> {
                    val getManageScoreResponse = state.data

                    if (getManageScoreResponse.total.totalEngineScore != 0.0) {
                        tv_recent_score2.text =
                            transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                        tv_engine_score.text =
                            transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                        if (getManageScoreResponse.diffAverage.totalEngineScore == 0.0) {
                            tv_recent_info_text.text = "점수 변동이 없어요"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                        } else if (getManageScoreResponse.diffAverage.totalEngineScore > 0.0) {
                            tv_recent_info_text.text =
                                "굉장해요. 지난 주행보다 +" + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점을 얻었어요!"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                        } else if (getManageScoreResponse.diffAverage.totalEngineScore < 0.0) {
                            tv_recent_info_text.text =
                                "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                        }

                    } else {
                        tv_recent_score2.text = "0"
                        tv_engine_score.text = "0"


                        tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
                is GetManageScoreState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
                is GetManageScoreState.Empty -> {

                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        tv_recent_driving_score = findViewById(R.id.tv_recent_driving_score)
        btn_back = findViewById(R.id.btn_back)
        btn_back.setOnClickListener{
            finish()
        }

        tv_recent_date = findViewById(R.id.tv_recent_date)
        tv_subtitle2 = findViewById(R.id.tv_subtitle2)
        tv_subtitle2.setOnClickListener {
        }

        tv_guide_subtitle = findViewById(R.id.tv_guide_subtitle)
        tv_guide_subtitle.setOnClickListener {
            startActivity(Intent(this@MyScoreActivity, DetectedStatusActivity::class.java))
        }


        tv_car_name = findViewById(R.id.tv_car_name)
        tv_car_no = findViewById(R.id.tv_car_no)
        layout_engine = findViewById(R.id.layout_engine)
        layout_engine.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyScoreActivity, ManageEngineActivity::class.java))
            }

        })


        layout_average_distance = findViewById(R.id.layout_average_distance)
        layout_average_time = findViewById(R.id.layout_average_time)

        layout_average_distance.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyScoreActivity, DrivenDistanceActivity::class.java))
            }

        })

        layout_average_time.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyScoreActivity, DrivenTimeActivity::class.java))
            }

        })



        button_average_score_overlay = findViewById(R.id.button_average_score_overlay)
        button_average_score_overlay.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyScoreActivity, DetailManageScoreActivity::class.java).putExtra("title","평균 관리 점수"))
            }

        })


        layout_recent_manage_score = findViewById(R.id.layout_recent_manage_score)
        layout_recent_manage_score.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MyScoreActivity, DetailManageScoreActivity::class.java).putExtra("title","최근 관리 점수"))
            }

        })

        tv_average_score = findViewById(R.id.tv_average_score)
        tv_increase = findViewById(R.id.tv_increase)
        tv_average_distance_contents = findViewById(R.id.tv_average_distance_contents)
        tv_average_time_contents = findViewById(R.id.tv_average_time_contents)

        view_diff_distance_background = findViewById(R.id.view_diff_distance_background)
        view_no_diff_distance = findViewById(R.id.view_no_diff_distance)
        view_there_is_diff_distance = findViewById(R.id.view_there_is_diff_distance)
        iv_there_is_diff_distance = findViewById(R.id.iv_there_is_diff_distance)
        tv_there_is_diff_distance = findViewById(R.id.tv_there_is_diff_distance)

        view_diff_time_background = findViewById(R.id.view_diff_time_background)
        view_no_diff_time = findViewById(R.id.view_no_diff_time)
        view_there_is_diff_time = findViewById(R.id.view_there_is_diff_time)
        iv_there_is_diff_time = findViewById(R.id.iv_there_is_diff_time)
        tv_there_is_diff_time = findViewById(R.id.tv_there_is_diff_time)

        tv_recent_score = findViewById(R.id.tv_recent_score)
        tv_recent_score2 = findViewById(R.id.tv_recent_score2)
        tv_recent_info_text = findViewById(R.id.tv_recent_info_text)
        iv_recent_info = findViewById(R.id.iv_recent_info)

        btn_recent = findViewById(R.id.btn_recent)
        btn_one_month = findViewById(R.id.btn_one_month)
        btn_six_month = findViewById(R.id.btn_six_month)
        btn_one_year = findViewById(R.id.btn_one_year)

        btn_recent.isSelected = true

        btn_recent.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = true
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "최근 주행 총점"

                myScoreViewModel.setRecentManageScoreForSummary(userCarId)
            }

        })

        btn_one_month.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = true
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "1개월 평균"

                myScoreViewModel.setManageSoreForSummary(29, userCarId)
            }

        })

        btn_six_month.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = true
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "6개월 평균"

                myScoreViewModel.setManageSoreForSummary(SIX_MONTH, userCarId)
            }

        })

        btn_one_year.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = true
                tv_recent_driving_score.text = "1년 평균"


                myScoreViewModel.setManageSoreForSummary(YEAR, userCarId)
            }
        })



        tv_engine_score = findViewById(R.id.tv_engine_score)
        iv_home_banner = findViewById(R.id.iv_home_banner)
        iv_home_banner.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                openChromeWithUrl("https://www.charancha.com/")
            }

        })

    }

    private fun setPieChart(percent:Float) {
        chart = findViewById(R.id.chart1)

        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(percent, ""))
        entries.add(PieEntry(100-percent, ""))


        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(ContextCompat.getColor(this, R.color.pie_gradient_end_color), ContextCompat.getColor(this, R.color.gray_50))
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        chart?.data = data

        // 차트 설정
        chart?.setTouchEnabled(false)
        chart?.setDrawHoleEnabled(true)
        chart?.setMaxAngle(180f) // Half chart
        chart?.setRotationAngle(180f) // Rotate to make it a half chart
        chart?.setHoleColor(Color.TRANSPARENT)
        chart?.setHoleRadius(70f)
        chart?.setTransparentCircleRadius(0f)
        chart?.description?.isEnabled = false
        chart?.legend?.isEnabled = false
        chart?.animateY(1000)
        chart?.invalidate()
        chart?.requestLayout()
    }

    private fun setLineChartForEngine(chart:LineChart){

        // 데이터 설정

        // 데이터 설정
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 0f))
        entries.add(Entry(1f, 32f))
        entries.add(Entry(2f, 25f))
        entries.add(Entry(3f, 31f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 26f))
        entries.add(Entry(6f, 21f))
        entries.add(Entry(7f, 14f))
        entries.add(Entry(8f, 23f))
        entries.add(Entry(9f, 32f))
        entries.add(Entry(10f, 12f))
        entries.add(Entry(11f, 29f))
        entries.add(Entry(12f, 1f))
        entries.add(Entry(13f, 5f))
        entries.add(Entry(14f, 17f))
        entries.add(Entry(15f, 9f))

        val dataSet = LineDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(this, R.color.pri_600)
        dataSet.lineWidth = 1f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 베지어 곡선 활성화

        dataSet.setDrawFilled(true) // 영역 색칠 활성화


        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.fade_red_engine) // 영역 색상으로 설정


        dataSet.fillAlpha = 50 // 영역 투명도 지정

        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        val lineData = LineData(dataSet)
        chart.setData(lineData)

        // 차트 설정

        // 차트 설정
        chart.getDescription().setEnabled(false)
        chart.setTouchEnabled(false)
        chart.setDragEnabled(false)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.getLegend().setEnabled(false)

        chart.setExtraOffsets(0f, 0f, 0f, 0f);

        val xAxis: XAxis = chart.getXAxis()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isEnabled = false


        val yAxisLeft: YAxis = chart.getAxisLeft()
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 50f
        yAxisLeft.isEnabled = false


        chart.getAxisRight().setDrawLabels(false)
        chart.getAxisRight().setAxisMaximum(100f)
        chart.getAxisRight().setDrawAxisLine(false)
        chart.getAxisRight().setDrawGridLines(false)
        chart.animateX(1500)
        chart.invalidate()

    }

    private fun setLineChartForTire(chart:LineChart){

        // 데이터 설정

        // 데이터 설정
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 0f))
        entries.add(Entry(1f, 32f))
        entries.add(Entry(2f, 25f))
        entries.add(Entry(3f, 31f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 26f))
        entries.add(Entry(6f, 21f))
        entries.add(Entry(7f, 14f))
        entries.add(Entry(8f, 23f))
        entries.add(Entry(9f, 32f))
        entries.add(Entry(10f, 12f))
        entries.add(Entry(11f, 29f))
        entries.add(Entry(12f, 1f))
        entries.add(Entry(13f, 5f))
        entries.add(Entry(14f, 17f))
        entries.add(Entry(15f, 9f))

        val dataSet = LineDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(this, R.color.gray_400)
        dataSet.lineWidth = 1f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 베지어 곡선 활성화

        dataSet.setDrawFilled(true) // 영역 색칠 활성화


        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.fade_red_tire) // 영역 색상으로 설정


        dataSet.fillAlpha = 50 // 영역 투명도 지정

        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        val lineData = LineData(dataSet)
        chart.setData(lineData)

        // 차트 설정

        // 차트 설정
        chart.getDescription().setEnabled(false)
        chart.setTouchEnabled(false)
        chart.setDragEnabled(false)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.getLegend().setEnabled(false)

        chart.setExtraOffsets(0f, 0f, 0f, 0f);

        val xAxis: XAxis = chart.getXAxis()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isEnabled = false


        val yAxisLeft: YAxis = chart.getAxisLeft()
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 50f
        yAxisLeft.isEnabled = false


        chart.getAxisRight().setDrawLabels(false)
        chart.getAxisRight().setAxisMaximum(100f)
        chart.getAxisRight().setDrawAxisLine(false)
        chart.getAxisRight().setDrawGridLines(false)
        chart.animateX(1500)
        chart.invalidate()
    }

    private fun setLineChartForBrakes(chart:LineChart){
        // 데이터 설정
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 0f))
        entries.add(Entry(1f, 32f))
        entries.add(Entry(2f, 25f))
        entries.add(Entry(3f, 31f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 26f))
        entries.add(Entry(6f, 21f))
        entries.add(Entry(7f, 14f))
        entries.add(Entry(8f, 23f))
        entries.add(Entry(9f, 32f))
        entries.add(Entry(10f, 12f))
        entries.add(Entry(11f, 29f))
        entries.add(Entry(12f, 1f))
        entries.add(Entry(13f, 5f))
        entries.add(Entry(14f, 17f))
        entries.add(Entry(15f, 9f))
        entries.add(Entry(15f, 9f))
        entries.add(Entry(15f, 49f))
        entries.add(Entry(15f, 4f))
        entries.add(Entry(15f, 33f))
        entries.add(Entry(15f, 12f))
        entries.add(Entry(15f, 12f))
        entries.add(Entry(15f, 1f))




        val dataSet = LineDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(this, R.color.gray_400)
        dataSet.lineWidth = 1f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // 베지어 곡선 활성화

        dataSet.setDrawFilled(true) // 영역 색칠 활성화


        dataSet.fillDrawable = ContextCompat.getDrawable(this, R.drawable.fade_red_brakes) // 영역 색상으로 설정


        dataSet.fillAlpha = 50 // 영역 투명도 지정

        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        val lineData = LineData(dataSet)
        chart.setData(lineData)

        // 차트 설정

        // 차트 설정
        chart.getDescription().setEnabled(false)
        chart.setTouchEnabled(false)
        chart.setDragEnabled(false)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.getLegend().setEnabled(false)


        val xAxis: XAxis = chart.getXAxis()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isEnabled = false


        val yAxisLeft: YAxis = chart.getAxisLeft()
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 50f
        yAxisLeft.isEnabled = false


        chart.getAxisRight().setDrawLabels(false)
        chart.getAxisRight().setAxisMaximum(100f)
        chart.getAxisRight().setDrawAxisLine(false)
        chart.getAxisRight().setDrawGridLines(false)
        chart.animateX(1500)
        chart.invalidate()
    }

    fun openChromeWithUrl(url:String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            startActivity(intent)
        }
    }

    fun formatDate(inputDate: String): String {
        // 입력 형식 정의
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // 출력 형식 정의
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())

        return try {
            // 입력 문자열을 Date로 파싱
            val date = inputFormat.parse(inputDate)
            // Date를 출력 형식으로 포맷팅
            outputFormat.format(date)
        } catch (e: Exception) {
            // 변환 실패 시 처리 (예: 입력 형식이 잘못되었을 때)
            inputDate
        }
    }
}