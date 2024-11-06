package com.milelog.retrofit.request

import com.milelog.room.dto.EachGpsDtoForApi

data class PostDrivingInfoRequest(
    val userCarId:String?,
    val startTimestamp:Long,
    val endTimestamp:Long,
    val verification:String,
    val gpses:List<EachGpsDtoForApi>
)