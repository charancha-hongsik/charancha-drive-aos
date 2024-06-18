package com.charancha.drive.retrofit.response

data class SignInResponse (
    val access_token:String,
    val refresh_token:String,
    val expires_in:String,
    val refresh_expires_in:String,
    val token_type:String
)