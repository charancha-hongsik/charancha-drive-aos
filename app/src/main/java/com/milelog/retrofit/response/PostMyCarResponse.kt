package com.milelog.retrofit.response

data class PostMyCarResponse (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    var carName:String,
    val modelYear:String,
    val releaseDt:String,
    var makerCd:String,
    var modelCd:String,
    var modelDetailCd:String,
    var gradeCd:String,
    var gradeDetailCd:String,
    val displacement:Int,
    var fuelCd:String,
    val trimHint:String
)