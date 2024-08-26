package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milelog.room.dto.EachGpsDtoForApi

// Alarm 데이터 저장용
@Entity(tableName = "alarmEntity")
data class AlarmEntity(
    @ColumnInfo(name="user_id") var user_id: String,
    @ColumnInfo(name="title") var title: String, // APP쪽에서의 임의 tracking_id
    @ColumnInfo(name="body") var body: String,
    @ColumnInfo(name="deepLink") var deepLink: String,
    @ColumnInfo(name="timestamp") var timestamp: String,
    @ColumnInfo(name="imageUrl") var imageUrl:String,
    @ColumnInfo(name="type") var type: String, // NOTICE, MARKETING, DRIVING
    @ColumnInfo(name="isRequired") var isRequired: Boolean // NOTICE, MARKETING, DRIVING
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
