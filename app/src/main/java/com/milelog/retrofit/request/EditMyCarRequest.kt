package com.milelog.retrofit.request

data class EditMyCarRequest (
    val licensePlateNumber:String,
    val ownerName:String,
    val carYear:Int,
    val carName:String,
    val fuel:String,
) : java.io.Serializable