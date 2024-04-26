package com.charancha.drive.room


// 시작했을 떄 클래스 생성
data class DriveDto(
    var tracking_id: String, // 20240417190026
    var timeStamp: Long, // 시작 시간
    var rank: String, // A,B,C,S
    var distance: Float, // 12533.736734857148
    var time: Long, // 2015.6142109632492
    var maxSpeed:Float,
    var rapid1: Int, // 0,1,2
    var rapid2: Int, // 0,1,2
    var rawData: List<EachGpsDto> // 원시 데이터
)