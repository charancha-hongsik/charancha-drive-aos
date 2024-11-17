package com.milelog.viewmodel.state

import com.milelog.retrofit.response.PatchCorpTypeResponse

sealed class PatchCorpTypeState {
    object Loading : PatchCorpTypeState()
    data class Success(val data: PatchCorpTypeResponse) : PatchCorpTypeState()
    data class Error(val code: Int, val message:String) : PatchCorpTypeState()
    object Empty : PatchCorpTypeState() // null 대신 사용
}