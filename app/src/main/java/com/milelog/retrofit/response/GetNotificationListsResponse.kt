package com.milelog.retrofit.response

data class GetNotificationListsResponse (
    val meta: Meta,
    val items:List<GetNotificationItem>,
)