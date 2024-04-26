package com.charancha.drive.room

import androidx.room.PrimaryKey


// 매번 쌓이는 데이터
data class EachGpsDto(
    var timeStamp: Long,
    var latitude: Double,
    var longtitude: Double,
    var speed: Float,
    var distance:Float,
    var acceleration: Float
)