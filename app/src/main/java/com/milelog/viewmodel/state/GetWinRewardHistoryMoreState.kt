package com.milelog.viewmodel.state

import WinRewardHistoryResponse
import com.milelog.retrofit.response.GetDrivingStatisticsResponse

sealed class GetWinRewardHistoryMoreState {
    object Loading : GetWinRewardHistoryMoreState()
    data class Success(val data: WinRewardHistoryResponse) : GetWinRewardHistoryMoreState()
    data class Error(val code: Int, val message:String) : GetWinRewardHistoryMoreState()
    object Empty : GetWinRewardHistoryMoreState() // null 대신 사용
}