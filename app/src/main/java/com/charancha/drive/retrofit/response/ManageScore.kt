package com.charancha.drive.retrofit.response

data class ManageScore(
    val totalScore:Double,
    val totalBrakeScore:Double,
    val totalEngineScore:Double,
    val totalTireScore:Double,
    val brakeScore:BreakScore,
    val engineScore:EngineScore,
    val tireScore:EngineScore,
)