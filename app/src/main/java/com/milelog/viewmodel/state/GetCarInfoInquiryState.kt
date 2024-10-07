package com.milelog.viewmodel.state

sealed class GetCarInfoInquiryState {
    object Loading : GetCarInfoInquiryState()
    data class Success(val data: String) : GetCarInfoInquiryState()
    data class Error(val code: Int, val message:String) : GetCarInfoInquiryState()
    object Empty : GetCarInfoInquiryState() // null 대신 사용
}