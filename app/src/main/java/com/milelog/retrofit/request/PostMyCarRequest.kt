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
    val fuelCd:String,
    val typeInput:TypeInput
) : java.io.Serializable

data class TypeInput(
    val type:String,
    val data:Data?
)

data class Data(
    // PERSONAL
    // CORPORATE
    val name:String,
    val department:String
)