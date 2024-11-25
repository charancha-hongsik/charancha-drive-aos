package com.milelog.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.milelog.BuildConfig
import com.milelog.PreferenceUtil
import com.milelog.R
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import com.milelog.retrofit.request.PostConnectDeviceRequest
import com.milelog.retrofit.request.PostDeviceInfoRequest
import com.milelog.retrofit.request.PostDrivingInfoRequest
import com.milelog.retrofit.response.PostConnectDeviceResponse
import com.milelog.retrofit.response.SignInResponse
import com.milelog.service.BluetoothService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseActivity: AppCompatActivity(){
    var distance_unit = "km"
    var SIX_MONTH = 155L
    var YEAR = 340L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        distance_unit = PreferenceUtil.getPref(this@BaseActivity,  PreferenceUtil.KM_MILE, "km")!!
    }

    override fun onResume() {
        super.onResume()
        distance_unit = PreferenceUtil.getPref(this@BaseActivity,  PreferenceUtil.KM_MILE, "km")!!

    }

    fun dpToPx(context: Context, dp: Int): Int {
        val resources = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
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

    fun convertUtcToDaysSinceForInt(utcTimeStr: String): Int {
        // UTC 시간 파싱
        val utcTime = LocalDateTime.parse(utcTimeStr, DateTimeFormatter.ISO_DATE_TIME)

        // 현재 한국 시간
        val currentKstTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

        // 일수 차이 계산
        val daysBetween = ChronoUnit.DAYS.between(utcTime, currentKstTime)

        return daysBetween.toInt() + 1
    }

    fun setFcmToken(tokenProcessCallback: LoginActivity.TokenProcessCallback) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            try {
                if (task.isComplete) {
                    val token = task.result
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(
                        {
                            Log.d("tetsetsetset","testsetsetes token1 :: " + token)
                            if(!PreferenceUtil.getBooleanPref(this@BaseActivity, PreferenceUtil.POST_DEVICE_INFO_STATE, false)){
                                val postDeviceInfoRequest = PostDeviceInfoRequest(
                                    manufacturer = Build.MANUFACTURER,
                                    model = Build.MODEL,
                                    os = "AOS",
                                    osVersion = Build.VERSION.SDK_INT.toString(),
                                    deviceType = "PHONE",
                                    appVersion = BuildConfig.VERSION_NAME,
                                    fcmDeviceToken = token
                                )

                                val gson = GsonBuilder().serializeNulls().create()
                                val jsonParam = gson.toJson(postDeviceInfoRequest)

                                apiService().postDeviceInfo(jsonParam.toRequestBody("application/json".toMediaTypeOrNull())).enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        if (response.code() == 200 || response.code() == 201){
                                            val postConnectDeviceResponse = gson.fromJson(response.body()?.string(), PostConnectDeviceResponse::class.java)

                                            PreferenceUtil.putBooleanPref(this@BaseActivity, PreferenceUtil.POST_DEVICE_INFO_STATE, true)
                                            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.DEVICE_ID_FOR_FCM, postConnectDeviceResponse.id)
                                        }else if(response.code() == 401){
                                            logout()
                                        }

                                        tokenProcessCallback.completeProcess()

                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                        tokenProcessCallback.completeProcess()

                                    }
                                })
                            }else{
                                tokenProcessCallback.completeProcess()
                            }
                        },
                        5000
                    )
                }
            } catch (e: java.lang.Exception) {
            }
        }
    }


    fun apiService(readTimeOut:Long = 30): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(this))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_API_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }

    fun apiService(url:String = BuildConfig.BASE_API_URL, readTimeOut:Long = 30): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(this))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(url).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(
                ApiServiceInterface::class.java
            )
    }


    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
        // 현재 시간 구하기 (ISO 8601)
        var now = Instant.now()

        // 현재 시간 기준 주어진 일 전 시간 구하기
        var previousDate = now

        if(past == 29L){
            previousDate = now.minus(past, ChronoUnit.DAYS)
        }

        if(past == SIX_MONTH){
            // Instant를 LocalDate로 변환합니다.
            val zoneId = ZoneId.systemDefault()
            val localDate = previousDate.atZone(zoneId).toLocalDate().minusMonths(5)


            // 월의 첫 번째 날을 구합니다.
            val firstDayOfMonth = localDate


            // LocalDate를 ZonedDateTime으로 변환합니다.
            val zonedDateTime = firstDayOfMonth.atStartOfDay(zoneId)


            // ZonedDateTime을 Instant로 변환합니다.
            previousDate = zonedDateTime.toInstant()

        }

        if(past == YEAR){
            // Instant를 LocalDate로 변환합니다.
            val zoneId = ZoneId.systemDefault()
            val localDate = previousDate.atZone(zoneId).toLocalDate().minusMonths(11)


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
        val zoneId = ZoneId.of("Asia/Seoul")
        val startDate = ZonedDateTime.ofInstant(previousDate, zoneId).toLocalDate()
        val endDate = ZonedDateTime.ofInstant(now, zoneId).toLocalDate()

        val resultList = mutableListOf<String>()
        val dateFormatter = DateTimeFormatter.ofPattern("MM월 dd일")
        val monthFormatter = DateTimeFormatter.ofPattern("MM월")

        var date = startDate

        if (past == SIX_MONTH || past == YEAR) {
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


            if(startDateTime.year != endDateTime.year){
                // 원하는 포맷으로 날짜를 변환
                val formatterForStart = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
                val formatterForEnd = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
                val formattedStartDate = startDateTime.format(formatterForStart)
                val formattedEndDate = endDateTime.format(formatterForEnd)

                // 포맷된 날짜를 합쳐서 반환
                return "$formattedStartDate ~ $formattedEndDate"
            }else{
                // 원하는 포맷으로 날짜를 변환
                val formatterForStart = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
                val formatterForEnd = DateTimeFormatter.ofPattern("M월", Locale.KOREAN)
                val formattedStartDate = startDateTime.format(formatterForStart)
                val formattedEndDate = endDateTime.format(formatterForEnd)

                // 포맷된 날짜를 합쳐서 반환
                return "$formattedStartDate ~ $formattedEndDate"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // 예외 처리: 날짜 형식이 올바르지 않은 경우 빈 문자열 반환 또는 예외 처리 로직 추가
            return ""
        }
    }

    fun formatDateRangeForDriveHistory(startDate: String, endDate: String): String {
        try {
            // 시작 날짜와 종료 날짜를 OffsetDateTime 객체로 파싱
            val startOffsetDateTime = OffsetDateTime.parse(startDate)
            val endOffsetDateTime = OffsetDateTime.parse(endDate)

            // OffsetDateTime 객체를 로컬 시간대로 변환 (한국 시간대로 설정 예시)
            val koreaZoneId = ZoneId.of("Asia/Seoul")
            val startDateTime = startOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()
            val endDateTime = endOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()


            if(startDateTime.year != endDateTime.year){
                // 원하는 포맷으로 날짜를 변환
                val formatterForStart = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
                val formatterForEnd = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
                val formattedStartDate = startDateTime.format(formatterForStart)
                val formattedEndDate = endDateTime.format(formatterForEnd)

                // 포맷된 날짜를 합쳐서 반환
                return "$formattedStartDate ~ $formattedEndDate"
            }else{
                // 원하는 포맷으로 날짜를 변환
                val formatterForStart = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
                val formatterForEnd = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
                val formattedStartDate = startDateTime.format(formatterForStart)
                val formattedEndDate = endDateTime.format(formatterForEnd)

                // 포맷된 날짜를 합쳐서 반환
                return "$formattedStartDate ~ $formattedEndDate"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // 예외 처리: 날짜 형식이 올바르지 않은 경우 빈 문자열 반환 또는 예외 처리 로직 추가
            return ""
        }
    }




    fun formatDateRangeForAMonth(startDate: String, endDate: String): String {
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

    fun formatDateRangeForAMonthForDriveHistory(startDate: String, endDate: String): String {
        try {
            // 시작 날짜와 종료 날짜를 OffsetDateTime 객체로 파싱
            val startOffsetDateTime = OffsetDateTime.parse(startDate)
            val endOffsetDateTime = OffsetDateTime.parse(endDate)

            // OffsetDateTime 객체를 로컬 시간대로 변환 (한국 시간대로 설정 예시)
            val koreaZoneId = ZoneId.of("Asia/Seoul")
            val startDateTime = startOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()
            val endDateTime = endOffsetDateTime.atZoneSameInstant(koreaZoneId).toLocalDateTime()

            // 원하는 포맷으로 날짜를 변환
            val startFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
            val endFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)

            val formattedStartDate = startDateTime.format(startFormatter)
            val formattedEndDate = endDateTime.format(endFormatter)

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
            val startOfMonthUTC = startOfMonth.atOffset(ZoneOffset.UTC).minusHours(9).format(DateTimeFormatter.ISO_INSTANT)

            // 월의 마지막 날
            val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
            val endOfMonthUTC = endOfMonth.atOffset(ZoneOffset.UTC).minusHours(9).format(DateTimeFormatter.ISO_INSTANT)

            Log.d("testsetsetset","testsetsetsetset dateStr :: " + dateStr)
            Log.d("testsetsetset","testsetsetsetset startOfMonthUTC :: " + startOfMonthUTC)
            Log.d("testsetsetset","testsetsetsetset endOfMonthUTC :: " + endOfMonthUTC)



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

    fun dpToPx(dp: Float): Int {
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

    fun logout(){
        if(PreferenceUtil.getPref(this@BaseActivity,PreferenceUtil.REFRESH_TOKEN,"") != "") {
            PreferenceUtil.getPref(this@BaseActivity, PreferenceUtil.DEVICE_ID_FOR_FCM, "")?.let {

                val gson = GsonBuilder().serializeNulls().create()
                val jsonParam =
                    gson.toJson(PostConnectDeviceRequest(it))


                apiService().postDisconnectDevice(
                    "Bearer " + PreferenceUtil.getPref(
                        this@BaseActivity,
                        PreferenceUtil.ACCESS_TOKEN,
                        ""
                    ), makeRequestBody(jsonParam)
                ).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.code() == 200 || response.code() == 201) {

                        }
                    }

                    override fun onFailure(
                        call: Call<ResponseBody>,
                        t: Throwable
                    ) {
                    }

                })
            }


            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.ACCESS_TOKEN, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.REFRESH_TOKEN, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.EXPIRES_IN, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.REFRESH_EXPIRES_IN, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.TOKEN_TYPE, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.KEYLESS_ACCOUNT, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.KEYLESS_ACCOUNT_EXPIRE, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.OAUTH_PROVIDER, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.ID_TOKEN, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.ACCOUNT_ADDRESS, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.USER_CARID, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.USER_ID, "")
            PreferenceUtil.putPref(this@BaseActivity, PreferenceUtil.MY_CAR_ENTITIES, "")

            PreferenceUtil.putBooleanPref(this@BaseActivity, PreferenceUtil.HAVE_BEEN_HOME, false)


            if(isMyServiceRunning(BluetoothService::class.java)){
                val bluetoothIntent = Intent(this, BluetoothService::class.java)
                stopService(bluetoothIntent)
            }


            startActivity(
                Intent(
                    this@BaseActivity,
                    LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()



        }
    }

    fun showCustomToast(context: Context, message: String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_toast, null)

        // Set the text in the custom layout
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message

        // Create and display the toast
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }

    fun makeRequestBody(jsonParam:String):RequestBody{
        return jsonParam.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun getMonthStartEndInUTC(): Pair<String, String> {
        // 현재 달의 첫 번째 날
        val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
        val startOfMonthUTC = startOfMonth.atOffset(ZoneOffset.UTC).minusHours(9)
            .format(DateTimeFormatter.ISO_INSTANT)

        // 현재 달의 마지막 날
        val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
        val endOfMonthUTC = endOfMonth.atOffset(ZoneOffset.UTC).minusHours(9)
            .format(DateTimeFormatter.ISO_INSTANT)

        return Pair(startOfMonthUTC, endOfMonthUTC)
    }

    fun formatToKoreanDate(date: String): String {
        // 입력 값이 8자리인지 확인 (yyyymmdd)
        require(date.length == 8) { "Invalid date format. Expected yyyymmdd." }

        // 연도, 월, 일 추출
        val year = date.substring(0, 4)
        val month = date.substring(4, 6)
        val day = date.substring(6, 8)

        // 결과 문자열 생성
        return year + "년 " + month + "월 " + day + "일"
    }




}