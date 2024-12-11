package com.milelog.viewmodel.state

import WinRewardHistoryResponse

sealed class GetWinRewardHistoryState {
    object Loading : GetWinRewardHistoryState()
    data class Success(val data: WinRewardHistoryResponse) : GetWinRewardHistoryState()
    data class Error(val code: Int, val message:String) : GetWinRewardHistoryState()
    object Empty : GetWinRewardHistoryState() // null 대신 사용
}