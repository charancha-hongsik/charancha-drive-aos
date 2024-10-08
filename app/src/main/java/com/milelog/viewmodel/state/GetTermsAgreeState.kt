package com.milelog.viewmodel.state

import com.milelog.retrofit.response.TermsAgreeStatusResponse

sealed class GetTermsAgreeState {
    object Loading : GetTermsAgreeState()
    data class Success(val data: List<TermsAgreeStatusResponse>) : GetTermsAgreeState()
    data class Error(val code: Int, val message:String) : GetTermsAgreeState()
    object Empty : GetTermsAgreeState() // null 대신 사용
}