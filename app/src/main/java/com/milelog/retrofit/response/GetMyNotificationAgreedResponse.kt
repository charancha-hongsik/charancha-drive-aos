package com.milelog.retrofit.response

data class GetMyNotificationAgreedResponse(
    val meta: Meta,
    val items: List<GetMyNotificationAgreedItem>,
)