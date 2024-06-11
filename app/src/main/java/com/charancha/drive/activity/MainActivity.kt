package com.charancha.drive.activity

import android.Manifest.permission.*
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.PreferenceUtil.HAVE_BEEN_HOME
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.service.BluetoothService
import com.charancha.drive.service.CallApiService
import com.charancha.drive.viewmodel.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
class MainActivity : AppCompatActivity() {
    lateinit var btnHistory: ImageButton
    private val mainViewModel: MainViewModel by viewModels()

    lateinit var chart: PieChart
    lateinit var btn_edit:ImageButton
    var tv_car_name:TextView? = null

    private fun promptForBatteryOptimization() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Battery Optimization")
        builder.setMessage("이 앱은 올바르게 작동하기 위해 배터리 최적화에서 제외되어야 합니다. 이 앱을 배터리 최적화에서 제외하시겠습니까?")
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.show()
    }

    private fun requestBatteryOptimizationException() {
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {

        } else {
            promptForBatteryOptimization()
        }
    }

    override fun onResume() {
        super.onResume()
        if(tv_car_name != null && PreferenceUtil.getPref(this, PreferenceUtil.USER_NAME, "") != ""){
            tv_car_name!!.text = PreferenceUtil.getPref(this, PreferenceUtil.USER_NAME, "")
        }

        val bluetoothIntent = Intent(this, BluetoothService::class.java)
        startForegroundService(bluetoothIntent)

        val callApiIntent = Intent(this, CallApiService::class.java)
        startForegroundService(callApiIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBatteryOptimizationException()
        }

        setPieChart()
        setLineChartForBrakes(findViewById(R.id.chart_line_brakes))
        setLineChartForEngine(findViewById(R.id.chart_line_engine))
        setLineChartForTire(findViewById(R.id.chart_line_tire))


        // 홈화면 진입 여부 체크
        PreferenceUtil.putBooleanPref(this, HAVE_BEEN_HOME, true)

        if(allPermissionsGranted()){
            setBtn()

        } else{

        }

        setAlarm()

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
        btnHistory = findViewById(R.id.btn_history)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, MyDriveHistoryActivity::class.java))
        }

        btn_edit = findViewById(R.id.btn_edit)
        btn_edit.setOnClickListener{
            startActivity(Intent(this, InputNameActivity::class.java))
        }

        tv_car_name = findViewById(R.id.tv_car_name)
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
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION,
                    BLUETOOTH_CONNECT,
                    POST_NOTIFICATIONS
                ).apply {

                }.toTypedArray()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mutableListOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION,
                    BLUETOOTH_CONNECT
                ).apply {

                }.toTypedArray()
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    ACTIVITY_RECOGNITION
                ).apply {

                }.toTypedArray()
            } else {
                mutableListOf (
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ).apply {

                }.toTypedArray()
            }
    }

    private fun setPieChart(){

        chart = findViewById(R.id.chart1)

        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(70f, ""))
        entries.add(PieEntry(30f, ""))

        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(ContextCompat.getColor(this, R.color.gray_900), ContextCompat.getColor(this, R.color.gray_50))
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        chart?.setData(data)

        // 차트 설정
        chart?.setTouchEnabled(false)
        chart?.setDrawHoleEnabled(true)
        chart?.setMaxAngle(180f)
        chart?.setRotationAngle(180f)
        chart?.setHoleColor(Color.TRANSPARENT)
        chart?.setHoleRadius(70f)
        chart?.setTransparentCircleRadius(0f)
        chart?.getDescription()?.isEnabled = false
        chart?.getLegend()?.isEnabled = false
        chart?.animateY(1000)
        chart?.invalidate()
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
        dataSet.color = ContextCompat.getColor(this, R.color.sec_500)
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
        dataSet.color = ContextCompat.getColor(this, R.color.gray_950)
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
            // 알람이 트리거될 때 수행할 작업을 여기에 추가
            Log.d("testestsetest","testestsesetestestsetsetset AlarmReceiver")

            val bluetoothIntent = Intent(context, BluetoothService::class.java)
            context.startForegroundService(bluetoothIntent)
        }
    }

}