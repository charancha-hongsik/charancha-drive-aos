package com.milelog.retrofit.response

data class VWorldDetailResponse(
    val response: Response
)

data class Response(
    val service: Service,
    val status: String,
    val record: Record,
    val page: Page,
    val result: Result
)

data class Service(
    val name: String,
    val version: String,
    val operation: String,
    val time: String
)

data class Record(
    val total: String,
    val current: String
)

data class Page(
    val total: String,
    val current: String,
    val size: String
)

data class Result(
    val crs: String,
    val type: String,
    val items: List<Item>
)

data class Item(
    val id: String,
    val title: String,
    val category: String,
    val address: Address,
    val point: Point
)

data class Address(
    val road: String,
    val parcel: String
)

data class Point(
    val x: String,
    val y: String
)