package com.charancha.drive.retrofit.response

data class GetAccountResponse(
    val id:String,
    val email:String,
    val createdAt:String,
    val deletedAt:String,
    val updatedAt:String,
    val provider:ProviderResponse
)