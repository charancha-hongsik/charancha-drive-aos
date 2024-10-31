package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetMyCarInfoItem

sealed class CarInfoInquiryByCarIdState {
    object Loading : CarInfoInquiryByCarIdState()
    data class Success(val data: GetMyCarInfoItem) : CarInfoInquiryByCarIdState()
    data class Error(val code: Int, val message:String) : CarInfoInquiryByCarIdState()
    object Empty : CarInfoInquiryByCarIdState() // null 대신 사용
}