package com.milelog.retrofit.response

data class GetDriveHistoryResponse(
    val meta: Meta,
    val items:MutableList<DriveItem>,
)