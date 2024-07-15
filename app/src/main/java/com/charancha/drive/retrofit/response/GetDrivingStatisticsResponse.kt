package com.charancha.drive.retrofit.response

data class GetDrivingStatisticsResponse(
    val total:List<GetDrivingInfoResponse>,
    val max:List<GetDrivingInfoResponse>,
    val min:List<GetDrivingInfoResponse>,
    val average:List<GetDrivingInfoResponse>,
    val perOne:List<GetDrivingInfoResponse>,
    val diffTotal:List<GetDrivingInfoResponse>,
    val diffMax:List<GetDrivingInfoResponse>,
    val diffMin:List<GetDrivingInfoResponse>,
    val diffAverage:List<GetDrivingInfoResponse>,
    val diffPerOne:List<GetDrivingInfoResponse>,
)