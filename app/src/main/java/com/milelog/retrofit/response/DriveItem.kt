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
    val endAddress:com.milelog.retrofit.request.Address?,
    val highSpeedDrivingDistance:Double,
    val lowSpeedDrivingDistance:Double,
    val highSpeedDrivingDistancePercentage:Double,
    val lowSpeedDrivingDistancePercentage:Double,
    val highSpeedDrivingMaxSpeed:Double,
    val averageSpeed:Double,
    val maxSpeed:Double,
    val lowSpeedDrivingMaxSpeed:Double,
    val rapidAccelerationCount:Int,
    val rapidDecelerationCount:Int,
    val rapidStartCount:Int,
    val rapidStopCount:Int,
    val optimalDrivingDistance:Double,
    val harshDrivingDistance:Double,
    val optimalDrivingPercentage:Double,
    val harshDrivingPercentage:Double,








    )