package com.milelog.retrofit.request

data class PostMyCarRequest (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val modelYear:String,
    val carName:String,
    val fuel:String,
) : java.io.Serializable