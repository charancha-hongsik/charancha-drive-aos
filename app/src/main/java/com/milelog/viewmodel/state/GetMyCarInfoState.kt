package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetMyCarInfoResponse

sealed class GetMyCarInfoState {
    object Loading : GetMyCarInfoState()
    data class Success(val data: Int) : GetMyCarInfoState()
    data class Error(val code: Int, val message:String) : GetMyCarInfoState()
    object Empty : GetMyCarInfoState() // null 대신 사용
}