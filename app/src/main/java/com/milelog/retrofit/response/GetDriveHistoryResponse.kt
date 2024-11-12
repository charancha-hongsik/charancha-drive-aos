package com.milelog.retrofit.response

data class NewDriveHistoryResponse(
    val date:String,
    val items:MutableList<DriveItem>
)

data class GetDriveHistoryResponse(
    val meta: Meta,
    val items:MutableList<DriveItem>,
)