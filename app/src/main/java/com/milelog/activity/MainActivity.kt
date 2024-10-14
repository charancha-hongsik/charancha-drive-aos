package com.milelog.activity

import android.Manifest.permission.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.milelog.CommonUtil
import com.milelog.CommonUtil.isBluetoothDeviceConnected
import com.milelog.CustomDialog
import com.milelog.FindBluetoothEntity
import com.milelog.PreferenceUtil
import com.milelog.PreferenceUtil.HAVE_BEEN_HOME
import com.milelog.R
import com.milelog.room.entity.DetectUserEntity
import com.milelog.service.BluetoothService
import com.milelog.service.BluetoothService.Companion.L2
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.MainViewModel
import com.milelog.viewmodel.state.AccountState
import com.milelog.viewmodel.state.CarInfoInquiryByCarIdState
import com.milelog.viewmodel.state.GetDrivingStatisticsState
import com.milelog.viewmodel.state.GetManageScoreState
import com.milelog.viewmodel.state.MyCarInfoState
import com.milelog.viewmodel.state.NotSavedDataState
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
class MainActivity : BaseRefreshActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    lateinit var btnHistory: ImageView

    lateinit var chart: PieChart
    lateinit var button_edit_overlay:Button
    lateinit var layout_engine: ConstraintLayout
    lateinit var layout_average_distance:ConstraintLayout
    lateinit var layout_average_time:ConstraintLayout
    lateinit var button_average_score_overlay:Button
    lateinit var layout_recent_manage_score:ConstraintLayout
    lateinit var tv_car_name:TextView
    lateinit var tv_car_no:TextView
    lateinit var tv_app_days2:TextView
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
    lateinit var btn_close_gift:ImageView
    lateinit var btn_noti:ImageView
    lateinit var tv_subtitle2:TextView

    lateinit var tv_guide_subtitle:TextView

    lateinit var layout_start_app:ConstraintLayout

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    var checkingUserActivityPermission = false
    var checkingIgnoreBatteryPermission = false

    override fun onResume() {
        super.onResume()

        mainViewModel.getMyCarInfo()
        /**
         * 사용자에게 위치권한을 받은 후 앱으로 돌아왔을 때에 대한 동작
         */
        setNextPermissionProcess()
        setBluetoothService()
        mainViewModel.postDrivingInfoNotSavedData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        mainViewModel.init(applicationContext)
        setObserver()

        mainViewModel.getAccount()
    }

    private fun init(){
        // FirebaseAnalytics 인스턴스 초기화
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setPieChart(0.0f)

        setLineChartForBrakes(findViewById(R.id.chart_line_brakes))
        setLineChartForEngine(findViewById(R.id.chart_line_engine))
        setLineChartForTire(findViewById(R.id.chart_line_tire))

        if(!PreferenceUtil.getBooleanPref(this, HAVE_BEEN_HOME, false)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission(mutableListOf(
                    BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray(),0)
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocation()
            } else{
                if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                    checkUserActivity()
                }else{
                    setIgnoreBattery()
                }
            }
        }

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        setBtn()

        if(intent.getBooleanExtra("deeplink",false)){
            startActivity(Intent(this@MainActivity, AlarmActivity::class.java))
        }
    }

    private fun setObserver(){
        mainViewModel.accountResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is AccountState.Loading -> {

                }
                is AccountState.Success -> {
                    val getAccountResponse = state.data
                    tv_app_days2.text = convertUtcToDaysSince(getAccountResponse.createdAt)
                    if(convertUtcToDaysSinceForInt(getAccountResponse.createdAt) > 14){
                        layout_start_app.visibility = GONE
                    }

                    PreferenceUtil.putPref(this@MainActivity, PreferenceUtil.USER_ID, getAccountResponse.id)
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

        mainViewModel.notSavedDataStateResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is NotSavedDataState.Error -> {
                    if(state.code == 401){
                        logout()
                    }
                }
            }
        })

        mainViewModel.myCarInfoResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is MyCarInfoState.Loading -> {

                }
                is MyCarInfoState.Success -> {
                    val getMyCarInfoResponses = state.data
                    if(getMyCarInfoResponses.size > 0){
                        mainViewModel.getCarInfoinquiryByCarId(getMyCarInfoResponses.get(0).id)
                    }else{
                        startActivity(Intent(this@MainActivity, SplashActivity::class.java))
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

        mainViewModel.carInfoInquiryByCarId.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is CarInfoInquiryByCarIdState.Loading -> {

                }
                is CarInfoInquiryByCarIdState.Success -> {
                    val getMyCarInfoResponse = state.data
                    PreferenceUtil.putPref(this@MainActivity, PreferenceUtil.USER_CARID, getMyCarInfoResponse.id)
                    tv_car_name.setText(getMyCarInfoResponse.carName)
                    tv_car_no.setText(getMyCarInfoResponse.licensePlateNumber)

                    mainViewModel.getManageScoreForAMonth()
                    mainViewModel.getDrivingDistanceForAMonth()
                    mainViewModel.setRecentManageScoreForSummary()
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

        mainViewModel.managerScoreResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
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
                        tv_increase.setTextColor(resources.getColor(R.color.pri_500))
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

                }
            }
        })

        mainViewModel.drivingStatisticsResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
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
                        tv_there_is_diff_distance.setTextColor(resources.getColor(R.color.pri_500))


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
                        tv_there_is_diff_time.setTextColor(resources.getColor(R.color.pri_500))


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

        mainViewModel.recentManageScoreResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
            when (state) {
                is GetManageScoreState.Loading -> {

                }
                is GetManageScoreState.Success -> {
                    val getManageScoreResponse = state.data

                    if (getManageScoreResponse.isRecent) {
                        if (getManageScoreResponse.total.totalEngineScore != 0.0) {
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
                            tv_recent_score2.text = "0"
                            tv_engine_score.text = "0"

                            tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
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

        mainViewModel.manageScoreForSummaryResult.observe(this@MainActivity, BaseViewModel.EventObserver{ state ->
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

    fun checkLocation(){
        CustomDialog(this, "위치 정보 권한", "위치 서비스를 사용할 수 없습니다. 기기의 ‘마일로그 > 권한 > 위치”에서 위치 서비스를 “항상 허용\"으로 켜주세요 (필수 권한)", "설정으로 이동","취소",  object : CustomDialog.DialogCallback{
            override fun onConfirm() {
                val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                checkingUserActivityPermission = true
                startActivity(openSettingsIntent)
            }

            override fun onCancel() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if(ContextCompat.checkSelfPermission(this@MainActivity, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                        checkUserActivity()
                    }else{
                        setIgnoreBattery()
                    }
                }
            }

        }).show()
    }

    fun checkUserActivity(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                CustomDialog(
                    this,
                    "신체 활동",
                    "신체 활동 서비스를 사용할 수 없습니다. 기기의 ‘마일로그 > 권한 > 신체 활동”에서 신체 활동을 “허용\" 으로 켜주세요 (필수 권한)",
                    "설정으로 이동",
                    "취소",
                    object : CustomDialog.DialogCallback {
                        override fun onConfirm() {
                            val openSettingsIntent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    val uri: Uri = Uri.fromParts("package", packageName, null)
                                    data = uri
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            checkingIgnoreBatteryPermission = true
                            startActivity(openSettingsIntent)
                        }

                        override fun onCancel() {
                            setIgnoreBattery()
                        }

                    }).show()
            }
        }
    }

    private fun setIgnoreBattery(){
        val i = Intent()
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        if(!pm.isIgnoringBatteryOptimizations(packageName)) {
            i.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            i.data = Uri.parse("package:$packageName")

            startActivity(i)
        }

        checkingIgnoreBatteryPermission = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        tv_recent_driving_score = findViewById(R.id.tv_recent_driving_score)
        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, MyPageActivity::class.java))
            }

        })

        tv_subtitle2 = findViewById(R.id.tv_subtitle2)
        tv_subtitle2.setOnClickListener {
            startActivity(Intent(this@MainActivity, FindBluetoothActivity::class.java))
        }

        tv_guide_subtitle = findViewById(R.id.tv_guide_subtitle)
        tv_guide_subtitle.setOnClickListener {
            startActivity(Intent(this@MainActivity, DetectedStatusActivity::class.java))
        }

        button_edit_overlay = findViewById(R.id.button_edit_overlay)
        button_edit_overlay.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, EditCarInfoActivity::class.java))
            }

        })


        tv_car_name = findViewById(R.id.tv_car_name)
        tv_car_no = findViewById(R.id.tv_car_no)
        layout_engine = findViewById(R.id.layout_engine)
        layout_engine.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, ManageEngineActivity::class.java))
            }

        })


        layout_average_distance = findViewById(R.id.layout_average_distance)
        layout_average_time = findViewById(R.id.layout_average_time)

        layout_average_distance.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DrivenDistanceActivity::class.java))
            }

        })

        layout_average_time.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DrivenTimeActivity::class.java))
            }

        })



        button_average_score_overlay = findViewById(R.id.button_average_score_overlay)
        button_average_score_overlay.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DetailManageScoreActivity::class.java).putExtra("title","평균 관리 점수"))
            }

        })


        layout_recent_manage_score = findViewById(R.id.layout_recent_manage_score)
        layout_recent_manage_score.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DetailManageScoreActivity::class.java).putExtra("title","최근 관리 점수"))
            }

        })

        tv_app_days2 = findViewById(R.id.tv_app_days2)
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
        layout_start_app = findViewById(R.id.layout_start_app)
        btn_close_gift = findViewById(R.id.btn_close_gift)

        btn_noti = findViewById(R.id.btn_noti)
        btn_noti.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, AlarmActivity::class.java))
            }

        })

        btn_recent.isSelected = true

        btn_recent.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = true
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "최근 주행 총점"

                mainViewModel.setRecentManageScoreForSummary()
            }

        })

        btn_one_month.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = true
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "1개월 평균"

                mainViewModel.setManageSoreForSummary(29)
            }

        })

        btn_six_month.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = true
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "6개월 평균"

                mainViewModel.setManageSoreForSummary(SIX_MONTH)
            }

        })

        btn_one_year.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = true
                tv_recent_driving_score.text = "1년 평균"


                mainViewModel.setManageSoreForSummary(YEAR)
            }
        })



        tv_engine_score = findViewById(R.id.tv_engine_score)
        iv_home_banner = findViewById(R.id.iv_home_banner)
        iv_home_banner.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                openChromeWithUrl("https://www.charancha.com/")
            }

        })

        btn_close_gift.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                PreferenceUtil.putBooleanPref(this@MainActivity,
                    PreferenceUtil.GIFT_EXPORTED, false)

                layout_start_app.visibility = GONE
            }

        })

        if(PreferenceUtil.getBooleanPref(this, PreferenceUtil.GIFT_EXPORTED, true)){
            layout_start_app.visibility = VISIBLE
        }else{
            layout_start_app.visibility = GONE
        }
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

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }

    private fun setNextPermissionProcess(){
        if(checkingUserActivityPermission){
            if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                checkingIgnoreBatteryPermission = true
                checkUserActivity()
            } else{
                setIgnoreBattery()
            }

            checkingUserActivityPermission = false
        }

        if(checkingIgnoreBatteryPermission){
            setIgnoreBattery()
        }
    }

    private fun setBluetoothService(){
        if(CommonUtil.checkRequiredPermissions(this@MainActivity)){
            if(!isMyServiceRunning(BluetoothService::class.java)){
                val bluetoothIntent = Intent(this, BluetoothService::class.java)
                startForegroundService(bluetoothIntent)
            }
        }else{
            if(isMyServiceRunning(BluetoothService::class.java)){
                val bluetoothIntent = Intent(this, BluetoothService::class.java)
                stopService(bluetoothIntent)
            }
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            0 -> {
                for(permission in permissions){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        checkPermission(mutableListOf(
                            POST_NOTIFICATIONS
                        ).apply {

                        }.toTypedArray(),1)
                    }else{
                        setIgnoreBattery()
                    }
                }
            }

            1->{
                setIgnoreBattery()
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}