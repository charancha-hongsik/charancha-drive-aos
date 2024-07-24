package com.charancha.drive.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.R
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseActivity: AppCompatActivity(){
    var distance_unit = "km"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        distance_unit = PreferenceUtil.getPref(this@BaseActivity,  PreferenceUtil.KM_MILE, "km")!!
    }

    override fun onResume() {
        super.onResume()
        Log.d("testsetestseset","teststssetst ::  " + PreferenceUtil.getPref(this@BaseActivity,  PreferenceUtil.KM_MILE, "km")!!)
        distance_unit = PreferenceUtil.getPref(this@BaseActivity,  PreferenceUtil.KM_MILE, "km")!!

    }

    fun apiService(): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl("http://43.201.46.37:3001/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }

    fun showKeyboard(editText: EditText) {
        // EditText에 포커스 주기
        editText.requestFocus()

        // InputMethodManager를 통해 키보드 올리기
        val imm = editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun transferDistanceWithUnit(meters:Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.0fkm", meters / 1000)
        }else{
            val milesPerMeter = 0.000621371
            return String.format(Locale.KOREAN, "%.0fmile",meters * milesPerMeter)
        }
    }

    fun transferDistance(meters:Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.0f", meters / 1000)
        }else{
            val milesPerMeter = 0.000621371
            return String.format(Locale.KOREAN, "%.0f",meters * milesPerMeter)
        }
    }

    fun transferNumWithRounds(percent:Double):Int{
        return String.format(Locale.KOREAN, "%.0f", percent).toInt()
    }


    fun getSpeedWithDistanceUnit(speed: Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.0fkm/h", speed)
        }else{
            val milesPerKilometer = 0.621371
            return String.format(Locale.KOREAN, "%.0fmile/h", speed * milesPerKilometer)
        }
    }

    fun transferSecondsToHourAndMinutes(seconds: Double): Pair<Int, Int> {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return Pair(hours, minutes)
    }

    fun secondsToHours(milliseconds: Double): Double {
        return milliseconds / 3600.0
    }

    fun secondsToMinutes(milliseconds: Double): Double {
        return (milliseconds / 60.0)
    }

    fun minutesToHours(minutes: Int): Double {
        return minutes / 60.0
    }


    fun getCurrentAndPastTimeForISO(past: Long): Triple<String, String, List<String>> {
        // 현재 시간 구하기
        val now = Instant.now()

        // 현재 시간 기준 주어진 일 전 시간 구하기
        var previousDate = now.minus(past, ChronoUnit.DAYS)

        if(past == 150L || past == 334L){
            // Instant를 LocalDate로 변환합니다.
            val zoneId = ZoneId.systemDefault()
            val localDate = previousDate.atZone(zoneId).toLocalDate()

            // 월의 첫 번째 날을 구합니다.
            val firstDayOfMonth = localDate.withDayOfMonth(1)

            // LocalDate를 ZonedDateTime으로 변환합니다.
            val zonedDateTime = firstDayOfMonth.atStartOfDay(zoneId)

            // ZonedDateTime을 Instant로 변환합니다.
            previousDate = zonedDateTime.toInstant()
        }

        // 시간 포맷
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

        // 포맷된 시간 문자열로 변환
        val nowFormatted = formatter.format(now)
        val previousDateFormatted = formatter.format(previousDate)

        // Instant를 ZonedDateTime으로 변환
        val zoneId = ZoneId.of("UTC")
        val startDate = ZonedDateTime.ofInstant(previousDate, zoneId).toLocalDate()
        val endDate = ZonedDateTime.ofInstant(now, zoneId).toLocalDate()

        val resultList = mutableListOf<String>()
        val dateFormatter = DateTimeFormatter.ofPattern("MM월 dd일")
        val monthFormatter = DateTimeFormatter.ofPattern("MM월")

        var date = startDate

        if (past == 150L || past == 334L) {
            // 범위 내 모든 달 찾기
            while (!date.isAfter(endDate)) {
                resultList.add(date.format(monthFormatter))
                date = date.plusMonths(1).withDayOfMonth(1)
            }
        } else if (past == 29L) {
            // 범위 내 모든 월요일 찾기
            while (!date.isAfter(endDate)) {
                if (date.dayOfWeek == DayOfWeek.MONDAY) {
                    resultList.add(date.format(dateFormatter))
                }
                date = date.plusDays(1)
            }
        }

        return Triple(nowFormatted, previousDateFormatted, resultList)
    }

    // 1번 형식의 데이터를 2번 형식의 데이터로 변환하는 함수
    fun convertDateFormat(dateString: String): String {
        try {
            // 입력된 문자열을 Instant 객체로 파싱
            val offsetDateTime = OffsetDateTime.parse(dateString)

            // OffsetDateTime 객체를 로컬 시간대로 변환 (한국 시간대로 설정 예시)
            val koreaZoneId = ZoneId.of("Asia/Seoul")
            val localDateTime = offsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()

            // 원하는 포맷으로 날짜를 변환
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
            return localDateTime.format(formatter)

        } catch (e: Exception) {
            e.printStackTrace()
            // 예외 처리: 날짜 형식이 올바르지 않은 경우 빈 문자열 반환 또는 예외 처리 로직 추가
            return ""
        }
    }

    fun formatDateRange(startDate: String, endDate: String): String {
        try {
            // 시작 날짜와 종료 날짜를 OffsetDateTime 객체로 파싱
            val startOffsetDateTime = OffsetDateTime.parse(startDate)
            val endOffsetDateTime = OffsetDateTime.parse(endDate)

            // OffsetDateTime 객체를 로컬 시간대로 변환 (한국 시간대로 설정 예시)
            val koreaZoneId = ZoneId.of("Asia/Seoul")
            val startDateTime = startOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()
            val endDateTime = endOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()

            // 원하는 포맷으로 날짜를 변환
            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
            val formattedStartDate = startDateTime.format(formatter)
            val formattedEndDate = endDateTime.format(formatter)

            // 포맷된 날짜를 합쳐서 반환
            return "$formattedStartDate ~ $formattedEndDate"

        } catch (e: Exception) {
            e.printStackTrace()
            // 예외 처리: 날짜 형식이 올바르지 않은 경우 빈 문자열 반환 또는 예외 처리 로직 추가
            return ""
        }
    }

    fun transformTimeToHHMM(isoDate: String):String{
        // UTC 시간 파싱
        val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

        // ZonedDateTime으로 변환
        val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

        // 한국 시간대로 변환 (UTC+9)
        val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

        // HH:mm 형식으로 변환
        val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        // 포맷된 문자열 반환
        return kstTimeStr
    }

    fun transformTimeToDate(isoDate: String):String{
        // UTC 시간 파싱
        val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

        // ZonedDateTime으로 변환
        val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

        // 한국 시간대로 변환 (UTC+9)
        val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

        // HH:mm 형식으로 변환
        val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))

        // 포맷된 문자열 반환
        return kstTimeStr
    }

    fun transformTimeToDateWithTime(isoDate: String):String{
        // UTC 시간 파싱
        val utcTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)

        // ZonedDateTime으로 변환
        val zonedUtcTime = utcTime.atZone(ZoneId.of("UTC"))

        // 한국 시간대로 변환 (UTC+9)
        val kstTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

        // HH:mm 형식으로 변환
        val kstTimeStr = kstTime.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일\nHH시 mm분 ss초"))

        // 포맷된 문자열 반환
        return kstTimeStr
    }

    fun getDateRange(dateStr:String):Pair<String, String>{
        // 입력된 문자열을 파싱하여 연도와 월을 추출
        val regex = Regex("(\\d{4})년 (\\d{1,2})월")
        val matchResult = regex.find(dateStr)

        if (matchResult != null) {
            val (year, month) = matchResult.destructured
            val yearInt = year.toInt()
            val monthInt = month.toInt()

            // 월의 첫 번째 날
            val startOfMonth = LocalDateTime.of(yearInt, monthInt, 1, 0, 0, 0, 0)
            val startOfMonthUTC = startOfMonth.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

            // 월의 마지막 날
            val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)
            val endOfMonthUTC = endOfMonth.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

            return Pair(endOfMonthUTC,startOfMonthUTC)
        } else {
            throw IllegalArgumentException("Invalid date format. Please use 'YYYY년 MM월'.")
        }
    }

    abstract class OnSingleClickListener: View.OnClickListener {
        private var lastClickTime = 0L

        abstract fun onSingleClick(v: View?)

        override fun onClick(v: View?) {
            val currentClickTime = System.currentTimeMillis()
            val checkTime = currentClickTime - lastClickTime

            lastClickTime = currentClickTime

            // 중복클릭이 아닌경우 onSingleClick 실행
            if (checkTime > 1000L) {
                onSingleClick(v)
            }
        }
    }


    fun showTooltipForHighLowEtc(context: Context) {
        // Create a BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_tooltip_high_low_etc, null)

        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set the close button action
        bottomSheetView.findViewById<TextView>(R.id.btn_set_mycar)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Show the dialog
        bottomSheetDialog.show()
    }

    fun showTooltipForEach(context: Context, title:String, contents:String) {
        // Create a BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialog)

        // Inflate the layout
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_tooltip_each, null)
        val tv_title = bottomSheetView.findViewById<TextView>(R.id.tv_tooltip_title)
        val tv_contents = bottomSheetView.findViewById<TextView>(R.id.tv_tooltip_contents)

        tv_title.text = title
        tv_contents.text = contents


        // Set the content view of the dialog
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set the close button action
        bottomSheetView.findViewById<TextView>(R.id.btn_set_mycar)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Show the dialog
        bottomSheetDialog.show()
    }



    private fun pxToDp(px: Float): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }

    fun getTodayFormattedDate(): String {
        // 현재 날짜를 가져옴
        val today = LocalDate.now()

        // 원하는 날짜 형식을 정의
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)

        // 현재 날짜를 지정된 형식으로 포맷
        return today.format(formatter)
    }



}