package com.milelog.retrofit.response

data class EngineScore (
    val totalScore:Double,
    val averageDrivingDistancePerOneScore:Double,
    val highSpeedDrivingScore:Double,
    val optimalDrivingScore:Double,
    val constantDrivingScore:Double,
    val rapidAccelerationDecelerationScore:Double
    )