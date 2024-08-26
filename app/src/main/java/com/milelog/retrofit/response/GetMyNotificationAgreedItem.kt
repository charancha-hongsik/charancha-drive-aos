package com.milelog.retrofit.response

data class GetMyNotificationAgreedItem (
    val id:String,
    val createdAt:String,
    val deletedAt:String,
    val updatedAt:String,
    val notificationId:String,
    val userId:String,
    val isAgreed:Boolean
)