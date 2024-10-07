package com.milelog.viewmodel.state

import com.milelog.retrofit.response.PatchDrivingResponse

sealed class PatchDrivingInfoState {
    object Loading : PatchDrivingInfoState()
    data class Success(val data: PatchDrivingResponse) : PatchDrivingInfoState()
    data class Error(val code: Int, val message:String) : PatchDrivingInfoState()
    object Empty : PatchDrivingInfoState() // null 대신 사용
}