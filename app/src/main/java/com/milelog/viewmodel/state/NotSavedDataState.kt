package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetAccountResponse

sealed class NotSavedDataState {
    object Loading : AccountState()
    data class Error(val code: Int, val message:String) : NotSavedDataState()
    object Empty : AccountState() // null 대신 사용
}