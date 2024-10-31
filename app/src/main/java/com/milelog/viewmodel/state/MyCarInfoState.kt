package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetMyCarInfoResponse

sealed class MyCarInfoState {
    object Loading : MyCarInfoState()
    data class Success(val data: GetMyCarInfoResponse) : MyCarInfoState()
    data class Error(val code: Int, val message:String) : MyCarInfoState()
    object Empty : MyCarInfoState() // null 대신 사용
}