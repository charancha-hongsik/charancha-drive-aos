package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milelog.room.dto.EachGpsDtoForApi

@Entity(tableName = "detectUserEntity")
data class DetectUserEntity(
    @ColumnInfo(name="user_id") var user_id: String,
    @ColumnInfo(name="verification") var verification: String,
    @ColumnInfo(name="start_stop") var start_stop: String,
    @ColumnInfo(name="timestamp") var timestamp: String,
    @ColumnInfo(name="sensor_state") var sensor_state: Boolean,

    ){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
