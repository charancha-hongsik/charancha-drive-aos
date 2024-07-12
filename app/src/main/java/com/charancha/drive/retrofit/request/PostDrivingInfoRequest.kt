package com.charancha.drive.retrofit.request

import com.charancha.drive.room.dto.EachGpsDtoForApi

data class PostDrivingInfoRequest(
    val userCarId:String,
    val startTimestamp:Long,
    val endTimestamp:Long,
    val verification:String,
    val gpses:List<EachGpsDtoForApi>
)