package com.charancha.drive.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive")
data class Drive(
    var date: Int,
    @field:PrimaryKey var timeStamp: Long,
    var latitude: Double,
    var longtitude: Double,
    var speed: Float,
    var distance:Float,
    var acceleration: Float
)