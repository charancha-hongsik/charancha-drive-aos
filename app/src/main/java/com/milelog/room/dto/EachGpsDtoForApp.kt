package com.milelog.room.dto

data class EachGpsDtoForApp(
    var timestamp:Long,
    var latitude: Double, // 위도
    var longtitude: Double, // 경도
    var altitude:Double, // 고도
) : java.io.Serializable