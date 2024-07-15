package com.charancha.drive.retrofit.response

data class GetDrivingGraphDataResponse (
    val meta:List<Meta>,
    val items:List<GraphItem>,
)