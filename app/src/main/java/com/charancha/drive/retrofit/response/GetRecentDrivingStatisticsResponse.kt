package com.charancha.drive.retrofit.response

data class GetRecentDrivingStatisticsResponse(
    val total:GetDrivingInfoResponse,
    val max:GetDrivingInfoResponse,
    val min:GetDrivingInfoResponse,
    val average:GetDrivingInfoResponse,
    val perOneTotal:GetDrivingInfoResponse,
    val perOneMax:GetDrivingInfoResponse,
    val perOneMin:GetDrivingInfoResponse,
    val perOneAverage:GetDrivingInfoResponse,
    val diffTotal:GetDrivingInfoResponse,
    val diffMax:GetDrivingInfoResponse,
    val diffMin:GetDrivingInfoResponse,
    val diffAverage:GetDrivingInfoResponse,
    val recentStartTime:String,
    val recentEndTime:String,
    val diffPerOneTotal:GetDrivingInfoResponse,
    val diffPerOneMax:GetDrivingInfoResponse,
    val diffPerOneMin:GetDrivingInfoResponse,
    val diffPerOneAverage:GetDrivingInfoResponse,
    val isRecent:Boolean
)