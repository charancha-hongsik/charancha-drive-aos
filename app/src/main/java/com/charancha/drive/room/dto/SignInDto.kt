package com.charancha.drive.room.dto

data class SignInDto (
    val idToken:String,
    val authorizationCode:String,
    val oauthProvider:String,
) : java.io.Serializable