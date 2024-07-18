package com.charancha.drive.retrofit.response

data class DriveItem (
    val id:String,
    val createdAt:String,
    val updatedAt:String,
    val userCarId:String,
    val verification:Double,
    val isActive:Boolean,
    val startTime:String,
    val endTime:String,
    val totalTime:Double,
    val totalDistance:Double
)