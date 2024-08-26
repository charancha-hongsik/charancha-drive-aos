package com.milelog.retrofit.request

import com.milelog.room.dto.EachGpsDtoForApi

data class PutNotificationAgreements(
    val notificationId:String,
    val isAgreed:Boolean
)