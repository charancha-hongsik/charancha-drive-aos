package com.milelog.viewmodel.state

import com.milelog.retrofit.request.PatchMemo
import com.milelog.retrofit.response.PatchDrivingResponse
import com.milelog.retrofit.response.PatchMemoResponse

sealed class PatchMemoState {
    object Loading : PatchMemoState()
    data class Success(val data: PatchMemoResponse) : PatchMemoState()
    data class Error(val code: Int, val message:String) : PatchMemoState()
    object Empty : PatchMemoState() // null 대신 사용
}