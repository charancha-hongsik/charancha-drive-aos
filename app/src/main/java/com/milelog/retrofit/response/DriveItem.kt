package com.milelog.retrofit.response

data class DriveItem (
    val id:String,
    val createdAt:String,
    val updatedAt:String,
    var userCarId:String?,
    val verification:String,
    var isActive:Boolean,
    val startTime:String,
    val endTime:String,
    val totalTime:Double,
    val totalDistance:Double,
    var userCar:UserCar?,
    val startAddress:com.milelog.retrofit.request.Address?,
    val endAddress:com.milelog.retrofit.request.Address?
    )