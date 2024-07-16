package com.charancha.drive.retrofit.response

data class GetRecentDrivingStatisticsResponse(
    val total:GetDrivingInfoResponse,
    val max:GetDrivingInfoResponse,
    val min:GetDrivingInfoResponse,
    val average:GetDrivingInfoResponse,
    val perOne:GetDrivingInfoResponse,
    val diffTotal:GetDrivingInfoResponse,
    val diffMax:GetDrivingInfoResponse,
    val diffMin:GetDrivingInfoResponse,
    val diffAverage:GetDrivingInfoResponse,
    val diffPerOne:GetDrivingInfoResponse,
    val recentStartTime:String,
    val recentEndTime:String,
    val isRecent:Boolean
)