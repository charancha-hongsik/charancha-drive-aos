package com.milelog.retrofit.request

data class PostDeviceInfoRequest(
    val manufacturer:String,
    val model:String,
    val os:String,
    val osVersion:String,
    val deviceType:String,
    val appVersion:String,
    val fcmDeviceToken:String,
)