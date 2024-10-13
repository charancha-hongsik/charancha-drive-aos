package com.milelog.retrofit.response

data class TermsAgreeStatusResponse (
    val createdAt:String,
    val deletedAt:String,
    val updatedAt:String,
    val id:String,
    val termsId:String,
    val userId:String,
    val isAgreed:Boolean,
    val terms: Terms,
)