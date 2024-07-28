package com.milelog.retrofit.request

data class SignInRequest (
    val idToken:String,
    val authorizationCode:String,
    val oauthProvider:String,
) : java.io.Serializable