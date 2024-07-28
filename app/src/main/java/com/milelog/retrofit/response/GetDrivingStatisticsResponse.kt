package com.milelog.retrofit.response

data class GetDrivingStatisticsResponse(
    val total: GetDrivingInfoResponse,
    val max: GetDrivingInfoResponse,
    val min: GetDrivingInfoResponse,
    val average: GetDrivingInfoResponse,
    val diffTotal: GetDrivingInfoResponse,
    val diffMax: GetDrivingInfoResponse,
    val diffMin: GetDrivingInfoResponse,
    val diffAverage: GetDrivingInfoResponse,
    val perOneTotal: GetDrivingInfoResponse,
    val perOneMax: GetDrivingInfoResponse,
    val perOneMin: GetDrivingInfoResponse,
    val perOneAverage: GetDrivingInfoResponse,
    val diffPerOneTotal: GetDrivingInfoResponse,
    val diffPerOneMax: GetDrivingInfoResponse,
    val diffPerOneMin: GetDrivingInfoResponse,
    val diffPerOneAverage: GetDrivingInfoResponse,
    )