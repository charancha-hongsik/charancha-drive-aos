package com.milelog.retrofit.response

data class GraphItem (
    val startTime:String,
    val endTime:String,
    val totalDistance:Double,
    val time:Double,
    val highSpeedDrivingDistancePercentage:Double,
    val lowSpeedDrivingDistancePercentage:Double,
    val etcSpeedDrivingDistancePercentage:Double,
    val constantSpeedDrivingDistancePercentage:Double,
    val optimalDrivingDistancePercentage:Double,
    val harshDrivingDistancePercentage:Double,
    val highSpeedDrivingDistance:Double,
    val lowSpeedDrivingDistance:Double,
    val etcSpeedDrivingDistance:Double,
    val constantSpeedDrivingDistance:Double,
    val rapidAccelerationDistance:Double,
    val optimalDrivingDistance:Double,
    val harshDrivingDistance:Double,


)