package com.milelog.retrofit.response
data class GetManageScoreResponse(
    val total: ManageScore,
    val max: ManageScore,
    val min: ManageScore,
    val average: ManageScore,
    val diffTotal: ManageScore,
    val diffMax: ManageScore,
    val diffMin: ManageScore,
    val diffAverage: ManageScore,
    val recentCriteriaAt:String,
    val isRecent:Boolean,
)
