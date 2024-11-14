package com.milelog.room

import androidx.room.TypeConverter
import com.milelog.room.dto.EachGpsDtoForApi
import com.milelog.room.dto.EachGpsDtoForApp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.milelog.retrofit.request.Address
import com.milelog.retrofit.request.Point

class Converters {
    @TypeConverter
    fun fromFloatList(value: List<Float>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toFloatList(value: String): List<Float> {
        return value.split(",").map { it.toFloat() }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }


    @TypeConverter
    fun fromEachGpsDtoList(value: List<EachGpsDtoForApp>?): String {
        return GsonBuilder().serializeNulls().create().toJson(value ?: emptyList<EachGpsDtoForApp>())
    }

    @TypeConverter
    fun toEachGpsDtoList(value: String?): List<EachGpsDtoForApp> {
        return if (value == null) emptyList() else GsonBuilder().serializeNulls().create().fromJson(value, object : TypeToken<List<EachGpsDtoForApp>>() {}.type)
    }

    @TypeConverter
    fun fromEachGpsDtoForApiList(value: List<EachGpsDtoForApi>?): String {
        return GsonBuilder().serializeNulls().create().toJson(value ?: emptyList<EachGpsDtoForApi>())
    }

    @TypeConverter
    fun toEachGpsDtoForApiList(value: String?): List<EachGpsDtoForApi> {
        return if (value == null) emptyList() else GsonBuilder().serializeNulls().create().fromJson(value, object : TypeToken<List<EachGpsDtoForApi>>() {}.type)
    }

    @TypeConverter
    fun fromString(value: String): List<List<Float>> {
        val listType = object : TypeToken<List<List<Float>>>() {}.type
        return GsonBuilder().serializeNulls().create().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<List<Float>>): String {
        return GsonBuilder().serializeNulls().create().toJson(list)
    }

    @TypeConverter
    fun fromPoint(point: Point): String {
        return "${point.x},${point.y}"
    }

    @TypeConverter
    fun toPoint(value: String): Point {
        val parts = value.split(",")
        return Point(parts[0], parts[1])
    }

    @TypeConverter
    fun fromAddress(address: Address?): String? {
        return address?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toAddress(json: String): Address? {
        return try {
            Gson().fromJson(json, Address::class.java)
        } catch (e: JsonSyntaxException) {
            null // 혹은 기본 Address 객체를 반환
        }
    }

}
