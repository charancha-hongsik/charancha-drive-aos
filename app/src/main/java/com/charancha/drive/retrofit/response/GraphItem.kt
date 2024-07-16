package com.charancha.drive.retrofit.response

data class GraphItem (
    val startTime:String,
    val endTime:String,
    val distance:Double,
    val time:Double,
    val highSpeedDrivingDistancePercentage:Double,
    val lowSpeedDrivingDistancePercentage:Double,
    val etcSpeedDrivingDistancePercentage:Double,
    val constantSpeedDrivingDistancePercentage:Double,
    val optimalDrivingDistancePercentage:Double,
    val harshDrivingDistancePercentage:Double,
)