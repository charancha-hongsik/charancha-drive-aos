package com.charancha.drive.retrofit.response

data class TermDetailResponse (
        val id:String,
        val createdAt:String,
        val deletedAt:String,
        val updatedAt:String,
        val title:String,
        val version:String,
        val content:String,
        val isActive:String,
        val isRequired:String
)