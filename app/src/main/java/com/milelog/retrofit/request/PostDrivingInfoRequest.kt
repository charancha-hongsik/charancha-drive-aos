package com.milelog.retrofit.request

import com.milelog.room.dto.EachGpsDtoForApi

data class PostDrivingInfoRequest(
    val userCarId:String?,
    val startTimestamp:Long,
    val endTimestamp:Long,
    val verification:String,
    val gpses:List<EachGpsDtoForApi>,
    val startAddress:Address,
    val endAddress:Address
)

data class Address(
    var point:Point,
    var road: Road?,
    var parcel: Parcel?,
    var places: List<Place>?
)

data class Road(
    var name: String
)

data class Parcel(
    var name: String
)

data class Place(
    var category: String,
    var name: String,
    var point: com.milelog.retrofit.request.Point,
    var address: PlaceAddress
)

data class Point(
    var x: String,
    var y: String
)

data class PlaceAddress(
    var road: Road,
    var parcel: Parcel
)