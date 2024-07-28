package com.milelog.retrofit.response

data class BreakScore (
    val totalScore:Double,
    val maximumLongTermParkingDurationScore:Double,
    val rapidDecelerationScore:Double
)