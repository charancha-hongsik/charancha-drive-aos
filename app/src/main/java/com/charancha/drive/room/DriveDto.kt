package com.charancha.drive.room

import androidx.room.PrimaryKey


// 시작했을 떄 클래스 생성
data class DriveDto(
    var tracking_id: String, // 20240417190026
    var timeStamp: Long, // 1714087621
    var verification: String, // L1, L2, L3, L4
    var distance_array: List<Float>, // 23개 시간대의 distance
    var time: Long, // 2015.6142109632492
    var sudden_deceleration_array: List<Int>, // 23개 시간대의 sudden_deceleration 갯수
    var sudden_stop_array: List<Int>, // 23개 시간대의 sudden_stop 갯수
    var sudden_acceleration_array: List<Int>, // 23개 시간대의 sudden_acceleration 갯수
    var sudden_start_array: List<Int>,  // 23개 시간대의 sudden_start 갯수
    var high_speed_driving_array: List<Float>, // 23개 시간대의 high_speed_driving 거리
    var low_speed_driving_array: List<Float>, // 23개 시간대의 low_speed_driving 거리
    var constant_speed_driving_array: List<Float>, // 23개 시간대의 constant_speed_driving 거리
    var harsh_driving_array:List<Float>, // 23개 시간대의 harsh_driving 거리
    var sum_sudden_deceleration_speed:Float,
    var jsonData: List<EachGpsDto> // 원시 데이터
) : java.io.Serializable