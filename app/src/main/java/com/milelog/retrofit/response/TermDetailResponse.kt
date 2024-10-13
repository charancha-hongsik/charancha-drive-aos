package com.milelog.retrofit.response

data class TermDetailResponse (
        val id:String,
        val createdAt:String,
        val deletedAt:String,
        val updatedAt:String,
        val title:String,
        val version:Int,
        val content:String,
        val isActive:Boolean,
        val isRequired:Boolean
)