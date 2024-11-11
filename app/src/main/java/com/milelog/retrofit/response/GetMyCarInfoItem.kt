package com.milelog.retrofit.response

import com.milelog.retrofit.request.Data

data class GetMyCarInfoItem(
    val id:String,
    val createdAt:String,
    val deletedAt:String,
    val updatedAt:String,
    val carId:String,
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carName:String,
    val modelYear:String,
    val releaseDt:String,
    val makerCd:String,
    val makerNm:String,
    val modelCd:String,
    val modelNm:String,
    val modelDetailCd:String,
    val modelDetailNm:String,
    val gradeCd:String,
    val gradeNm:String,
    val gradeDetailCd:String,
    val gradeDetailNm:String,
    val fuelCd:String,
    val fuelNm:String,
    val modelDetailImageUrl:String?,
    val userId:String?,
    val type:String,
    val data:Data
)