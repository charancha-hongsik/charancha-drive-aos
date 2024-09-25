package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetDriveHistoryResponse

sealed class GetDriveHistoryState {
    object Loading : GetDriveHistoryState()
    data class Success(val data: GetDriveHistoryResponse, val startTime:String, val endTime:String) : GetDriveHistoryState()
    data class Error(val code: Int, val message:String) : GetDriveHistoryState()
    object Empty : GetDriveHistoryState() // null 대신 사용
}