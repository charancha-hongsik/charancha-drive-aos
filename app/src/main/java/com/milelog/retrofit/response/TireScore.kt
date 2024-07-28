package com.milelog.retrofit.response

data class TireScore (
    val totalScore:Double,
    val maximumLongTermParkingDurationScore:Double,
    val rapidAccelerationScore:Double,
    val rapidDecelerationScore:Double
    )