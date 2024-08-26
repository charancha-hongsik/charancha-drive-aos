package com.milelog.retrofit.response

data class GetNotificationItem (
    val id:String,
    val createdAt:String,
    val deletedAt:String,
    val updatedAt:String,
    val name:String,
    val description:String,
    var topic:String,
    val isActive:Boolean,
)