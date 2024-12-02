package com.milelog.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.milelog.BuildConfig
import com.milelog.retrofit.ApiServiceInterface
import com.milelog.retrofit.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

open class BaseViewModel:ViewModel() {
    var distance_unit = "km"
    var SIX_MONTH = 155L
    var YEAR = 340L

    fun apiService(context: Context, readTimeOut:Long = 30): ApiServiceInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(context))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_API_URL).client(client)
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
            val firstDayOfMonth = localDate.withDayOfMonth(1)


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

    open class Event<out T>(private val content: T) {
        var hasBeenHandled = false
            private set

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }

    class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
        override fun onChanged(value: Event<T>) {
            value?.getContentIfNotHandled()?.let { value ->
                onEventUnhandledContent(value)
            }
        }
    }
}