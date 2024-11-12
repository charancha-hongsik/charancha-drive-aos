package com.milelog.retrofit.request

data class EditMyCarRequest (
    val licensePlateNumber:String?,
    val ownerName:String?,
    val carName:String?,
    val makerCd:String?,
    val modelCd:String?,
    val modelDetailCd:String?,
    val gradeCd:String?,
    val gradeDetailCd:String?,
    val fuelCd:String?,
    val typeInput:TypeInput?
    ) : java.io.Serializable