package com.charancha.drive.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charancha.drive.room.dto.EachGpsDtoForApp

/**
 * 성공 시 저장되는 데이터
 */
@Entity(tableName = "drive")
data class DriveForApp(
    @field:PrimaryKey var tracking_id: String,
    @ColumnInfo(name="startTimestamp") var startTimestamp:Long,
    @ColumnInfo(name="jsonData") var jsonData: List<EachGpsDtoForApp>
)
