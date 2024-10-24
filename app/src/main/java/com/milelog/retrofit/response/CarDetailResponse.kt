package com.milelog.retrofit.response

data class CarDetailResponse(
    val makerCd:String,
    val makerNm:String,
    val modelCd:String,
    val modelNm:String,
    val modelDetailCd:String,
    val modelDetailNm:String,
    val gradeCd:String,
    val gradeNm:String,
    val gradeDetailCd:String,
    val gradeDetailNm:String,
    val code:String, // 연료
    val codeNm:String // 연료
)