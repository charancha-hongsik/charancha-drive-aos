package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetDrivingStatisticsResponse

sealed class GetDrivingStatisticsState {
    object Loading : GetDrivingStatisticsState()
    data class Success(val data: GetDrivingStatisticsResponse) : GetDrivingStatisticsState()
    data class Error(val code: Int, val message:String) : GetDrivingStatisticsState()
    object Empty : GetDrivingStatisticsState() // null 대신 사용
}