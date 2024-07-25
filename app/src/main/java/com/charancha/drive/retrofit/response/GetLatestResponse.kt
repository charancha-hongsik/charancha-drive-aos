package com.charancha.drive.retrofit.response

data class GetLatestResponse(
    val os:String,
    val deviceType:String,
    val version:String,
    val serviceName:String,
    val forceUpdate:Boolean,
)