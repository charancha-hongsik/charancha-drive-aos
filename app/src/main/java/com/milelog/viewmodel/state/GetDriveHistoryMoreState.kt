package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetDriveHistoryResponse

sealed class GetDriveHistoryMoreState {
    object Loading : GetDriveHistoryMoreState()
    data class Success(val data: GetDriveHistoryResponse) : GetDriveHistoryMoreState()
    data class Error(val code: Int, val message:String) : GetDriveHistoryMoreState()
    object Empty : GetDriveHistoryMoreState() // null 대신 사용
}