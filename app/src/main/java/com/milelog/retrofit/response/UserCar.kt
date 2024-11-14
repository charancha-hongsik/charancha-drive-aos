package com.milelog.retrofit.response

import com.milelog.retrofit.request.Data

data class UserCar(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    var type: String,
    val licensePlateNumber: String,
    val ownerName: String,
    val modelYear: String,
    val releaseDt: String?,
    var carName: String,
    val makerCd: String,
    val makerNm: String,
    val modelCd: String,
    val modelNm: String,
    val modelDetailCd: String,
    val modelDetailNm: String,
    val fuelCd: String,
    val fuelNm: String,
    val data: Data
)