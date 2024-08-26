package com.milelog.retrofit.request

data class PatchDeviceInfoRequest(
    val osVersion:String,
    val appVersion:String,
    val fcmDeviceToken:String,
    val userId:String
)