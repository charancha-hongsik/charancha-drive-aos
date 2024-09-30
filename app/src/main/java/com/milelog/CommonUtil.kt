package com.milelog

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.app.Service.START_STICKY
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object CommonUtil {
    fun getSpannableString(context: Context, originalText:String, spanText:String, color:Int):SpannableString{

        val spannableString = SpannableString(originalText)

        val start = originalText.indexOf(spanText)
        val end = start + spanText.length

        val colorSpan = ForegroundColorSpan(color) // 원하는 색상으로 변경

        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    fun getAlarmDate(dateString:String):String{
        // 입력된 날짜를 ZonedDateTime으로 파싱
        val inputDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)

        // 현재 시간을 ZonedDateTime으로 가져오기 (기본적으로 시스템의 시간대)
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        // 입력된 시간과 현재 시간의 차이 계산
        val duration = Duration.between(inputDateTime, currentDateTime)

        return when {
            // 0시간 이상 ~ 1시간 미만
            duration.toHours() in 0..1 -> "조금 전"

            // 1시간 이상 ~ 24시간 미만
            duration.toHours() in 1..23 -> "${duration.toHours()}시간 전"

            // 24시간 이상 ~ 192시간 미만 (1일 이상 ~ 8일 미만)
            duration.toDays() in 1..7 -> "${duration.toDays()}일 전"

            // 8일 이상 (192시간 이상)
            else -> inputDateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        }
    }

    fun checkRequiredPermissions(context:Context):Boolean{
        if(ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(context, ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    return true
                }else{
                    return false
                }
            }else{
                return true
            }
        }else {
            return false
        }
    }
}