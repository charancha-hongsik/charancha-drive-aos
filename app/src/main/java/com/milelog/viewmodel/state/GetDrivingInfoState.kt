package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetDrivingInfoResponse

sealed class GetDrivingInfoState {
    object Loading : GetDrivingInfoState()
    data class Success(val data: GetDrivingInfoResponse) : GetDrivingInfoState()
    data class Error(val code: Int, val message:String) : GetDrivingInfoState()
    object Empty : GetDrivingInfoState() // null 대신 사용
}