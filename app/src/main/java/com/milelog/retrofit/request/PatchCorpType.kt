package com.milelog.retrofit.request

data class PatchCorpType (
    val userCarId:String,
    val isActive:Boolean,
    val type:String
) : java.io.Serializable