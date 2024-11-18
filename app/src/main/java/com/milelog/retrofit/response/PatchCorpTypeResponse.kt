package com.milelog.retrofit.response
data class PatchCorpTypeResponse(
    val type:String,
    val userCar: UserCar,
    val isActive:Boolean,
    val userCarId:String
)
