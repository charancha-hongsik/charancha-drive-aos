package com.charancha.drive.room.dto

data class DriveDtoForApi(
    var manufacturer:String,
    var version:String,
    var deviceModel:String,
    var deviceUuid:String,
    var username:String,
    var trackingId: String, // 20240417190026
    var startTimestamp: Long, // 1714087621
    var endTimestamp: Long,
    var verification:String,
    var appVersion: String,
    var gpses: List<EachGpsDtoForApi> // 원시 데이터
) : java.io.Serializable