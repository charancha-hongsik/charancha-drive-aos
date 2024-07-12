package com.charancha.drive.retrofit.response
data class GetMyCarInfoResponse (
    val id:String,
    val createdAt:String,
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carName:String,
    val carYear:Int,
    val fuel:String
)