package com.milelog.retrofit.request

data class EditMyCarRequest (
    val licensePlateNumber:String,
    val ownerName:String,
    val modelYear:String,
    val carName:String,
    val fuel:String,
) : java.io.Serializable