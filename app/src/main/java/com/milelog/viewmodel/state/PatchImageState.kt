package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetDrivingInfoResponse
import com.milelog.retrofit.response.PatchDrivingResponse

sealed class PatchImageState {
    object Loading : PatchImageState()
    data class Success(val data: GetDrivingInfoResponse) : PatchImageState()
    data class Error(val code: Int, val message:String) : PatchImageState()
    object Empty : PatchImageState() // null 대신 사용
}