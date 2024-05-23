package com.charancha.drive.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charancha.drive.room.EachGpsDto

@Entity(tableName = "drive")
data class Drive(
    @field:PrimaryKey var tracking_id: String, // 20240417190026
    @ColumnInfo(name="timeStamp") var timeStamp: Long, // 1714087621
    @ColumnInfo(name="verification") var verification: String, // L1, L2, L3, L4
    @ColumnInfo(name="distance_array") var distance_array: List<Float>, // 23개 시간대의 distance
    @ColumnInfo(name="time") var time: Long, // 2015.6142109632492
    @ColumnInfo(name="sudden_deceleration_array") var sudden_deceleration_array: List<Int>, // 23개 시간대의 sudden_deceleration 갯수
    @ColumnInfo(name="sudden_stop_array") var sudden_stop_array: List<Int>, // 23개 시간대의 sudden_stop 갯수
    @ColumnInfo(name="sudden_acceleration_array") var sudden_acceleration_array: List<Int>, // 23개 시간대의 sudden_acceleration 갯수
    @ColumnInfo(name="sudden_start_array") var sudden_start_array: List<Int>,  // 23개 시간대의 sudden_start 갯수
    @ColumnInfo(name="high_speed_driving_array") var high_speed_driving_array: List<Float>, // 23개 시간대의 high_speed_driving 거리
    @ColumnInfo(name="low_speed_driving_array") var low_speed_driving_array: List<Float>, // 23개 시간대의 low_speed_driving 거리
    @ColumnInfo(name="constant_speed_driving_array") var constant_speed_driving_array: List<Float>, // 23개 시간대의 constant_speed_driving 거리
    @ColumnInfo(name="harsh_driving_array") var harsh_driving_array:List<Float>, // 23개 시간대의 harsh_driving 거리
    @ColumnInfo(name="sum_sudden_deceleration_speed") var sum_sudden_deceleration_speed:Float,
    @ColumnInfo(name="jsonData") var jsonData: List<EachGpsDto> // 원시 데이터
)
