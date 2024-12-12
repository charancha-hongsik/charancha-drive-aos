package com.milelog.retrofit.request

data class FilterRequest(
    val field: String,
    val operator: String,
    val value: Any
)
