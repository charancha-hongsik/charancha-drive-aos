package com.milelog.viewmodel.state

import com.milelog.retrofit.response.SignInResponse

sealed class PostReissueState {
    object Loading : PostReissueState()
    data class Success(val data: SignInResponse) : PostReissueState()
    data class Error(val code: Int, val message:String) : PostReissueState()
    object Empty : PostReissueState() // null 대신 사용
}