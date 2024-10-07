package com.milelog.retrofit.response

data class PostMyCarResponse (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carName:String,
    val modelYear:String,
    val releaseDt:String,
    val makerCd:String,
    val modelCd:String,
    val modelDetailCd:String,
    val gradeCd:String,
    val gradeDetailCd:String,
    val displacement:Int,
    val fuelCd:String,
    val fuel:String,
    )