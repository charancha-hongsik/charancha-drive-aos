package com.charancha.drive.retrofit.response

data class GetDriveHistoryResponse(
    val meta:Meta,
    val items:List<DriveItem>,
)