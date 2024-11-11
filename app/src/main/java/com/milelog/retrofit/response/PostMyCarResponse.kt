package com.milelog.retrofit.response

import com.milelog.retrofit.request.Data

data class PostMyCarResponse (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    var carName:String,
    val modelYear:String,
    val releaseDt:String,
    var makerCd:String?,
    var makerNm:String?,
    var modelCd:String?,
    var modelNm:String?,
    var modelDetailCd:String?,
    var modelDetailNm:String?,
    var gradeCd:String?,
    var gradeNm:String?,
    var gradeDetailCd:String?,
    var gradeDetailNm:String?,
    val displacement:Int?,
    var fuelCd:String?,
    var fuelNm:String?,
    val trimHint:String?,
    val type:String,
    val data:Data
)