package com.charancha.drive.room.dto

data class EachGpsDtoForApi(
    var timestamp: Long,
    var speed: Float,
    var distance:Float,
    var altitude:Double,
    var acceleration: Float
) : java.io.Serializable