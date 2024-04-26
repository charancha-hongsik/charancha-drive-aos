package com.charancha.drive.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive")
data class Drive(
    @field:PrimaryKey var tracking_id: String, // 20240417190026
    var timeStamp: Long, // 1714087621
    var rank: String, // A,B,C,S
    var distance: Double, // 12533.736734857148
    var time: Double, // 2015.6142109632492
    var rapid1: Int, // 0,1,2
    var rapid2: Int, // 0,1,2
    var jsonData: String // 원시 데이터
)