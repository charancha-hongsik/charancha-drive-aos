package com.charancha.drive.retrofit.response

data class PostMyCarResponse (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carVender:String,
    val modelName:String,
    val carYear:String,
    val fuel:String
)