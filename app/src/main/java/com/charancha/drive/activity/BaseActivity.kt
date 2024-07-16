package com.charancha.drive.activity

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.charancha.drive.PreferenceUtil
import com.charancha.drive.retrofit.ApiServiceInterface
import com.charancha.drive.retrofit.HeaderInterceptor
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
            return String.format(Locale.KOREAN, "%.3fkm", meters / 1000)
        }else{
            val milesPerMeter = 0.000621371
            return String.format(Locale.KOREAN, "%.3fmile",meters * milesPerMeter)
        }
    }

    fun transferDistance(meters:Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.3f", meters / 1000)
        }else{
            val milesPerMeter = 0.000621371
            return String.format(Locale.KOREAN, "%.3f",meters * milesPerMeter)
        }
    }


    fun getSpeedWithDistanceUnit(speed: Double):String{
        if(distance_unit == "km"){
            return String.format(Locale.KOREAN, "%.3fkm/h", speed)
        }else{
            val milesPerKilometer = 0.621371
            return String.format(Locale.KOREAN, "%.3fmile/h", speed * milesPerKilometer)
        }
    }

    fun transferSecondsToHourAndMinutes(seconds: Double): Pair<Int, Int> {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return Pair(hours, minutes)
    }

    fun getCurrentAndPastTimeForISO(past:Long): Triple<String, String,List<String>> {
        // 현재 시간 구하기
        val now = Instant.now()

        // 현재 시간 기준 주어진 일 전 시간 구하기
        val previousDate = now.minus(past, ChronoUnit.DAYS)

        // 시간 포맷
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

        // 포맷된 시간 문자열로 변환
        val nowFormatted = formatter.format(now)
        val previousDateFormatted = formatter.format(previousDate)

        // Instant를 ZonedDateTime으로 변환
        val zoneId = ZoneId.of("UTC")
        val startDate = ZonedDateTime.ofInstant(previousDate, zoneId).toLocalDate()
        val endDate = ZonedDateTime.ofInstant(now, zoneId).toLocalDate()

        // 범위 내 모든 월요일 찾기
        val mondays = mutableListOf<String>()
        val dateFormatter = DateTimeFormatter.ofPattern("MM월 dd일")

        var date = startDate

        while (!date.isAfter(endDate)) {
            if (date.dayOfWeek == DayOfWeek.MONDAY) {
                mondays.add(date.format(dateFormatter))
            }
            date = date.plusDays(1)
        }

        return Triple(nowFormatted, previousDateFormatted, mondays)
    }

}