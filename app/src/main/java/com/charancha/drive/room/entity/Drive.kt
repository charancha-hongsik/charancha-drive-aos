package com.charancha.drive.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive")
class Drive(
    var date: String,
    @field:PrimaryKey var timeStamp: String,
    var latitude: String,
    var longtitude: String,
    var speed: String,
    var distance:String,
    var acceleration: String
)