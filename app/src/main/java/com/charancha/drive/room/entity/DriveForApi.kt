package com.charancha.drive.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charancha.drive.room.dto.EachGpsDtoForApi

@Entity(tableName = "driveForApi")
data class DriveForApi(
    @field:PrimaryKey var tracking_id: String, // APP쪽에서의 임의 tracking_id
    @ColumnInfo(name="userCarId") var userCarId: String,
    @ColumnInfo(name="startTimestamp") var startTimestamp: Long,
    @ColumnInfo(name="endTimestamp") var endTimestamp: Long,
    @ColumnInfo(name="verification") var verification:String,
    @ColumnInfo(name="gpses") var gpses: List<EachGpsDtoForApi>
)
