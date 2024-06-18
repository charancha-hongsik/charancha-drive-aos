package com.charancha.drive.room.dto

data class SignUpDto (
    val idToken:String,
    val authorizationCode:String,
    val oauthProvider:String,
    val deviceToken:String,
) : java.io.Serializable