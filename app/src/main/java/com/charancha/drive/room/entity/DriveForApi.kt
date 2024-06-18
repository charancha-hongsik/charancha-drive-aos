package com.charancha.drive.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charancha.drive.room.dto.EachGpsDtoForApi

@Entity(tableName = "driveForApi")
data class DriveForApi(
    @field:PrimaryKey var tracking_id: String, // 20240417190026
    @ColumnInfo(name="manufacturer") var manufacturer:String,
    @ColumnInfo(name="version") var version:String,
    @ColumnInfo(name="deviceModel") var deviceModel:String,
    @ColumnInfo(name="deviceUuid") var deviceUuid:String,
    @ColumnInfo(name="username") var username:String,
    @ColumnInfo(name="startTimeStamp") var startTimeStamp: Long, // 1714087621
    @ColumnInfo(name="endTimestamp") var endTimestamp: Long,
    @ColumnInfo(name="verification") var verification:String,
    @ColumnInfo(name="automobile") var automobile:Boolean,
    @ColumnInfo(name="appVersion") var appVersion: String, // 새로운 컬럼 추가
    @ColumnInfo(name="gpses") var gpses: List<EachGpsDtoForApi> // 원시 데이터
)
