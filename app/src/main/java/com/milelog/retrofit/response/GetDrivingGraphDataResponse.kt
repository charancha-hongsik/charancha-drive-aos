package com.milelog.retrofit.response

data class GetDrivingGraphDataResponse (
    val meta: Meta,
    val items:List<GraphItem>,
)