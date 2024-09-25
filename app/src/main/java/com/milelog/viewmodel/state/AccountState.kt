package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetAccountResponse

sealed class AccountState {
    object Loading : AccountState()
    data class Success(val data: GetAccountResponse) : AccountState()
    data class Error(val code: Int, val message:String) : AccountState()
    object Empty : AccountState() // null 대신 사용
}