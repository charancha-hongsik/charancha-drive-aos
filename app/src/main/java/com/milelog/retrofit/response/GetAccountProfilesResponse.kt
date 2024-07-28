package com.milelog.retrofit.response

data class GetAccountProfilesResponse(
    val id:String,
    val nickName:String,
    val createdAt:String,
    val deletedAt:String,
    val user: GetAccountResponse
)