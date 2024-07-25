package com.charancha.drive.activity

import android.Manifest.permission.*
import android.app.*
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.charancha.drive.*
import com.charancha.drive.PreferenceUtil.HAVE_BEEN_HOME
import com.charancha.drive.retrofit.response.GetAccountResponse
import com.charancha.drive.retrofit.response.GetDrivingStatisticsResponse
import com.charancha.drive.retrofit.response.GetManageScoreResponse
import com.charancha.drive.retrofit.response.GetMyCarInfoResponse
import com.charancha.drive.service.BluetoothService
import com.charancha.drive.service.CallApiService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    lateinit var btnHistory: ImageView

    lateinit var chart: PieChart
    lateinit var btn_edit:ImageView
    lateinit var layout_engine: ConstraintLayout
    lateinit var layout_average_distance:ConstraintLayout
    lateinit var layout_average_time:ConstraintLayout
    lateinit var tv_average_score_info:TextView
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



    var checkingPermission = false


    override fun onResume() {
        super.onResume()

        setCarInfo()

        val bluetoothIntent = Intent(this, BluetoothService::class.java)
        startForegroundService(bluetoothIntent)

        if(checkingPermission){
            if(ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                checkUserActivity()
            }

            checkingPermission = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val callApiIntent = Intent(this, CallApiService::class.java)
        startForegroundService(callApiIntent)

        setPieChart(0.0f)

        setLineChartForBrakes(findViewById(R.id.chart_line_brakes))
        setLineChartForEngine(findViewById(R.id.chart_line_engine))
        setLineChartForTire(findViewById(R.id.chart_line_tire))

        if(!PreferenceUtil.getBooleanPref(this, HAVE_BEEN_HOME, false)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                checkPermission(mutableListOf(
                    BLUETOOTH_CONNECT,
                    POST_NOTIFICATIONS
                ).apply {

                }.toTypedArray(),0)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                }
            }
        }

        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        setBtn()
        setAlarm()

        getAccount()
    }

    fun getAccount(){
        apiService().getAccount("Bearer " + PreferenceUtil.getPref(this@MainActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val getAccountResponse = Gson().fromJson(
                    response.body()?.string(),
                    GetAccountResponse::class.java
                )

                if(response.code() == 200 || response.code() == 201){
                    tv_app_days2.text = convertUtcToDaysSince(getAccountResponse.createdAt)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

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
                checkingPermission = true
                startActivity(openSettingsIntent)
            }

            override fun onCancel() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if(ContextCompat.checkSelfPermission(this@MainActivity, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
                        checkUserActivity()
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
                            startActivity(openSettingsIntent)
                        }

                        override fun onCancel() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    checkPermission(mutableListOf(
                                        BLUETOOTH_CONNECT,
                                        POST_NOTIFICATIONS
                                    ).apply {

                                    }.toTypedArray(), 0
                                    )
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    checkPermission(mutableListOf(
                                        BLUETOOTH_CONNECT
                                    ).apply {

                                    }.toTypedArray(), 0
                                    )
                                }
                            }
                        }

                    }).show()
            }
        }
    }



    private fun setAlarm(){
        var flag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_MUTABLE
        }

        // 알람 매니저 초기화
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 알람이 트리거될 때 브로드캐스트를 발생시킬 인텐트 생성
        val intent = Intent(this, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, flag)

        // 주기적으로 알람 예약
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 1)
            add(Calendar.HOUR_OF_DAY, 1)
        }

        // RTC_WAKEUP을 사용하여 디바이스를 깨웁니다.
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
              1 * 60 * 60 * 1000,
            alarmIntent
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtn(){
        tv_recent_driving_score = findViewById(R.id.tv_recent_driving_score)
        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, MyPageActivity::class.java))
            }

        })


        btn_edit = findViewById(R.id.btn_edit)
        btn_edit.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, EditCarInfoActivity::class.java))
            }

        })


        tv_car_name = findViewById(R.id.tv_car_name)
        tv_car_no = findViewById(R.id.tv_car_no)
        layout_engine = findViewById(R.id.layout_engine)
        layout_engine.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, ManageEngineActivity::class.java))
            }

        })


        layout_average_distance = findViewById(R.id.layout_average_distance)
        layout_average_time = findViewById(R.id.layout_average_time)

        layout_average_distance.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DrivenDistanceActivity::class.java))
            }

        })

        layout_average_time.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DrivenTimeActivity::class.java))
            }

        })

        tv_average_score_info = findViewById(R.id.tv_average_score_info)
        tv_average_score_info.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@MainActivity, DetailManageScoreActivity::class.java).putExtra("title","평균 관리 점수"))
            }

        })


        layout_recent_manage_score = findViewById(R.id.layout_recent_manage_score)
        layout_recent_manage_score.setOnClickListener(object:OnSingleClickListener(){
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

        btn_recent.isSelected = true

        btn_recent.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = true
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "최근 주행 총점"

                setRecentManageScoreForSummary()            }

        })

        btn_one_month.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = true
                btn_six_month.isSelected = false
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "1개월 평균"

                setManageSoreForSummary(29)
            }

        })

        btn_six_month.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = true
                btn_one_year.isSelected = false

                tv_recent_driving_score.text = "6개월 평균"

                setManageSoreForSummary(150)
            }

        })

        btn_one_year.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                btn_recent.isSelected = false
                btn_one_month.isSelected = false
                btn_six_month.isSelected = false
                btn_one_year.isSelected = true
                tv_recent_driving_score.text = "1년 평균"


                setManageSoreForSummary(334)
            }
        })



        tv_engine_score = findViewById(R.id.tv_engine_score)
        iv_home_banner = findViewById(R.id.iv_home_banner)
        iv_home_banner.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                openChromeWithUrl("https://www.charancha.com/")
            }

        })


    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    companion object {
        private val REQUIRED_PERMISSIONS =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                mutableListOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_BACKGROUND_LOCATION,
                    BLUETOOTH_CONNECT,
                    POST_NOTIFICATIONS,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mutableListOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_BACKGROUND_LOCATION,
                    BLUETOOTH_CONNECT,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_BACKGROUND_LOCATION,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else {
                mutableListOf (
                    ACCESS_FINE_LOCATION
                ).apply {

                }.toTypedArray()
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

    class AlarmReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bluetoothIntent = Intent(context, BluetoothService::class.java)
            context.startForegroundService(bluetoothIntent)
        }
    }

    private fun checkPermission(permissions: Array<String>, code: Int) {
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions,code)
            return
        }
    }

    fun setCarInfo(){
        apiService().getMyCarInfo("Bearer " + PreferenceUtil.getPref(this@MainActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                if(response.code() == 200 || response.code() == 201){
                    val jsonString = response.body()?.string()

                    val type: Type = object : TypeToken<List<GetMyCarInfoResponse?>?>() {}.type
                    val getMyCarInfoResponses:List<GetMyCarInfoResponse> = Gson().fromJson(jsonString, type)

                    if(getMyCarInfoResponses.size > 0){
                        apiService().getCarInfoinquiryByCarId("Bearer " + PreferenceUtil.getPref(this@MainActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!, getMyCarInfoResponses.get(0).id).enqueue(object :
                            Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if(response.code() == 200 || response.code() == 201){
                                    val getMyCarInfoResponse = Gson().fromJson(
                                        response.body()?.string(),
                                        GetMyCarInfoResponse::class.java
                                    )

                                    Log.d("testestest","testsetse :: " + getMyCarInfoResponse.id)

                                    PreferenceUtil.putPref(this@MainActivity, PreferenceUtil.USER_CARID, getMyCarInfoResponse.id)
                                    tv_car_name.setText(getMyCarInfoResponse.carName)
                                    tv_car_no.setText(getMyCarInfoResponse.vehicleIdentificationNumber)

                                    getManageScoreForAMonth()
                                    getDrivingDistanceForAMonth()
                                    setRecentManageScoreForSummary()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                            }

                        })
                    }else{
                        startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                        finish()
                    }
                }else{

                }
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {

            }
        })
    }

    fun convertUtcToDaysSince(utcTimeStr: String): String {
        // UTC 시간 파싱
        val utcTime = LocalDateTime.parse(utcTimeStr, DateTimeFormatter.ISO_DATE_TIME)

        // 현재 한국 시간
        val currentKstTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

        // 일수 차이 계산
        val daysBetween = ChronoUnit.DAYS.between(utcTime, currentKstTime)

        return "${daysBetween + 1}일째"
    }

    fun getManageScoreForAMonth(){
        apiService().getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@MainActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first).enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200 || response.code() == 201){
                    val getManageScoreResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetManageScoreResponse::class.java
                    )

                    tv_average_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                    if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
                        tv_increase.text = "변동 없음"
                        tv_increase.setTextColor(resources.getColor(R.color.gray_500))
                    }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
                        tv_increase.text = "+" + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 증가"
                        tv_increase.setTextColor(resources.getColor(R.color.pri_500))
                    }else if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
                        tv_increase.text = transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore).toString() + "점 하락"
                        tv_increase.setTextColor(resources.getColor(R.color.sec_500))
                    }

                    setPieChart((getManageScoreResponse.average.totalEngineScore/10).toFloat())

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun getDrivingDistanceForAMonth(){
        apiService().getDrivingStatistics(
            "Bearer " + PreferenceUtil.getPref(this@MainActivity, PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(29).second,
            getCurrentAndPastTimeForISO(29).first,
            "startTime",
            "day").enqueue(object:
            Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    val getDrivingStatisticsResponse = Gson().fromJson(
                        response.body()?.string(),
                        GetDrivingStatisticsResponse::class.java
                    )
                    tv_average_distance_contents.text = transferDistanceWithUnit(getDrivingStatisticsResponse.average.totalDistance)
                    tv_average_time_contents.text = transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.average.totalTime).first.toString() + "시간" + transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.average.totalTime).second.toString() + "분"

                    if(getDrivingStatisticsResponse.diffAverage.totalDistance == 0.0){
                        view_diff_distance_background.background = resources.getDrawable(R.drawable.radius999_gray100)
                        view_no_diff_distance.visibility = VISIBLE
                        view_there_is_diff_distance.visibility = GONE

                    }else if(getDrivingStatisticsResponse.diffAverage.totalDistance > 0.0){
                        view_diff_distance_background.background = resources.getDrawable(R.drawable.radius999_pri50)
                        view_no_diff_distance.visibility = GONE
                        view_there_is_diff_distance.visibility = VISIBLE
                        iv_there_is_diff_distance.setImageDrawable(resources.getDrawable(R.drawable.vector_pri))
                        tv_there_is_diff_distance.setText(transferDistanceWithUnit(getDrivingStatisticsResponse.diffAverage.totalDistance))
                        tv_there_is_diff_distance.setTextColor(resources.getColor(R.color.pri_500))


                    }else if(getDrivingStatisticsResponse.diffAverage.totalDistance < 0.0){
                        view_diff_distance_background.background = resources.getDrawable(R.drawable.radius999_sec50)
                        view_no_diff_distance.visibility = GONE
                        view_there_is_diff_distance.visibility = VISIBLE
                        iv_there_is_diff_distance.setImageDrawable(resources.getDrawable(R.drawable.vector_sec))
                        tv_there_is_diff_distance.setText(transferDistanceWithUnit(getDrivingStatisticsResponse.diffAverage.totalDistance))
                        tv_there_is_diff_distance.setTextColor(resources.getColor(R.color.sec_500))

                    }

                    if(getDrivingStatisticsResponse.diffAverage.totalTime == 0.0){
                        view_diff_time_background.background = resources.getDrawable(R.drawable.radius999_gray100)
                        view_no_diff_time.visibility = VISIBLE
                        view_there_is_diff_time.visibility = GONE


                    }else if(getDrivingStatisticsResponse.diffAverage.totalTime > 0.0){
                        view_diff_time_background.background = resources.getDrawable(R.drawable.radius999_pri50)
                        view_no_diff_time.visibility = GONE
                        view_there_is_diff_time.visibility = VISIBLE
                        iv_there_is_diff_time.setImageDrawable(resources.getDrawable(R.drawable.vector_pri))
                        tv_there_is_diff_time.setText(transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.diffAverage.totalTime).first.toString() + "시간" + transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.diffAverage.totalTime).second.toString() + "분")
                        tv_there_is_diff_time.setTextColor(resources.getColor(R.color.pri_500))



                    }else if(getDrivingStatisticsResponse.diffAverage.totalTime < 0.0){
                        view_diff_time_background.background = resources.getDrawable(R.drawable.radius999_sec50)
                        view_no_diff_time.visibility = GONE
                        view_there_is_diff_time.visibility = VISIBLE
                        iv_there_is_diff_time.setImageDrawable(resources.getDrawable(R.drawable.vector_sec))
                        tv_there_is_diff_time.setText(transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.diffAverage.totalTime).first.toString() + "시간" + transferSecondsToHourAndMinutes(getDrivingStatisticsResponse.diffAverage.totalTime).second.toString() + "분")
                        tv_there_is_diff_time.setTextColor(resources.getColor(R.color.sec_500))



                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setRecentManageScoreForSummary(){
        apiService().getRecentManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@MainActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@MainActivity, PreferenceUtil.USER_CARID, "")!!
        ).enqueue(object: Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                    if(getManageScoreResponse.isRecent){
                        if(getManageScoreResponse.total.totalEngineScore != 0.0){
                            tv_recent_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                            tv_recent_score2.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                            tv_engine_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                            if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
                                tv_recent_info_text.text = "점수 변동이 없어요"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                            }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
                                tv_recent_info_text.text = "굉장해요. 지난 주행보다 +" +  transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점을 얻었어요!"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                            }else if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
                                tv_recent_info_text.text = "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
                                iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                            }

                        }else{
                            tv_recent_score2.text = "0"
                            tv_engine_score.text = "0"

                            tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                        }
                    }else{
                        tv_recent_score2.text = "0"
                        tv_engine_score.text = "0"

                        tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    fun setManageSoreForSummary(scope:Long){
        apiService().getManageScoreStatistics(
            "Bearer " + PreferenceUtil.getPref(this@MainActivity,  PreferenceUtil.ACCESS_TOKEN, "")!!,
            PreferenceUtil.getPref(this@MainActivity, PreferenceUtil.USER_CARID, "")!!,
            getCurrentAndPastTimeForISO(scope).second,
            getCurrentAndPastTimeForISO(scope).first
        ).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if(response.code() == 200 || response.code() == 201){
                    val getManageScoreResponse = Gson().fromJson(response.body()?.string(), GetManageScoreResponse::class.java)
                    if(getManageScoreResponse.total.totalEngineScore != 0.0){
                        tv_recent_score2.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()
                        tv_engine_score.text = transferNumWithRounds(getManageScoreResponse.average.totalEngineScore).toString()

                        if(getManageScoreResponse.diffAverage.totalEngineScore == 0.0){
                            tv_recent_info_text.text = "점수 변동이 없어요"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_good))
                        }else if(getManageScoreResponse.diffAverage.totalEngineScore > 0.0){
                            tv_recent_info_text.text = "굉장해요. 지난 주행보다 +" +  transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점을 얻었어요!"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_love))
                        }else if(getManageScoreResponse.diffAverage.totalEngineScore < 0.0){
                            tv_recent_info_text.text = "아쉬워요. 지난 주행보다 " + transferNumWithRounds(getManageScoreResponse.diffAverage.totalEngineScore) + "점 하락했어요"
                            iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_crying))
                        }

                    }else{
                        tv_recent_score2.text = "0"
                        tv_engine_score.text = "0"


                        tv_recent_info_text.text = "아직 데이터가 없어요. 함께 달려볼까요?"
                        iv_recent_info.setImageDrawable(resources.getDrawable(R.drawable.resource_face_soso))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
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




}