package com.charancha.drive.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive2")
data class Drive(
    @field:PrimaryKey var tracking_id: String, // 20240417190026
    var timeStamp: Long, // 1714087621
    var verification: String, // L1, L2, L3, L4
    var distance: Float, // 12533.736734857148
    var time: Long, // 2015.6142109632492
    var sudden_deceleration: Int, // 0,1,2
    var sudden_stop: Int, // 0,1,2
    var sudden_acceleration: Int, // 0,1,2
    var sudden_start: Int, // 0,1,2
    var high_speed_driving: Float, // 12533.736734857148
    var low_speed_driving: Float, // 12533.736734857148
    var constant_speed_driving: Float, // 12533.736734857148
    var jsonData: String // 원시 데이터
)

