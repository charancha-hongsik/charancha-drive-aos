package com.milelog.retrofit.response

data class Meta(
    val itemCount:Int,
    val totalItems:Int,
    val hasNextPage:Boolean,
    var afterCursor:String?,
    var beforeCursor:String?
)