package com.charancha.drive.retrofit.request

data class PostMyCarRequest (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carYear:Int,
    val carVender:String,
    val modelName:String,
    val fuel:String,
) : java.io.Serializable