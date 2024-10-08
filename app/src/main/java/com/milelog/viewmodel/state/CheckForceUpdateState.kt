package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetAccountResponse

sealed class CheckForceUpdateState {
    object Loading : CheckForceUpdateState()
    data class Success(val isRequired: Boolean) : CheckForceUpdateState()
    data class Error(val code: Int, val message:String) : CheckForceUpdateState()
    object Empty : CheckForceUpdateState() // null 대신 사용
}