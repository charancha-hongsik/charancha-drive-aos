package com.charancha.drive.retrofit.request

data class SignUpRequest (
    val idToken:String,
    val authorizationCode:String,
    val oauthProvider:String,
    val deviceToken:String,
    val accountAddress:String
) : java.io.Serializable