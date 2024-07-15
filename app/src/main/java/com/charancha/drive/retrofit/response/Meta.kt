package com.charancha.drive.retrofit.response

data class Meta(
    val itemCount:Int,
    val totalItems:Int,
    val hasNextPage:Boolean,
    val afterCursor:String,
    val beforeCursor:String
)