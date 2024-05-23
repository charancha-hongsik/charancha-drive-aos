package com.charancha.drive.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    fun fromEachGpsDtoList(value: List<EachGpsDto>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toEachGpsDtoList(value: String): List<EachGpsDto> {
        val listType = object : TypeToken<List<EachGpsDto>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromString(value: String): List<List<Float>> {
        val listType = object : TypeToken<List<List<Float>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<List<Float>>): String {
        return Gson().toJson(list)
    }

}
