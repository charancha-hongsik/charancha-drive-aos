package com.milelog.retrofit.request

data class PostMyCarRequest (
    val licensePlateNumber:String,
    val ownerName:String,
    val vehicleIdentificationNumber:String,
    val carName:String,
    val makerCd:String,
    val modelCd:String,
    val modelDetailCd:String?,
    val gradeCd:String?,
    val gradeDetailCd:String?,
    val fuelCd:String
) : java.io.Serializable