package com.milelog.viewmodel.state

import com.milelog.retrofit.response.GetManageScoreResponse

sealed class GetManageScoreState {
    object Loading : GetManageScoreState()
    data class Success(val data: GetManageScoreResponse) : GetManageScoreState()
    data class Error(val code: Int, val message:String) : GetManageScoreState()
    object Empty : GetManageScoreState() // null 대신 사용
}