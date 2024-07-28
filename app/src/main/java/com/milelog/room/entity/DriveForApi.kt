package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milelog.room.dto.EachGpsDtoForApi

// API 호출 실패한 데이터 저장 용
@Entity(tableName = "driveForApi")
data class DriveForApi(
    @ColumnInfo(name="tracking_Id") var tracking_id: String, // APP쪽에서의 임의 tracking_id
    @ColumnInfo(name="userCarId") var userCarId: String,
    @ColumnInfo(name="startTimestamp") var startTimestamp: Long,
    @ColumnInfo(name="endTimestamp") var endTimestamp: Long,
    @ColumnInfo(name="verification") var verification:String,
    @ColumnInfo(name="gpses") var gpses: List<EachGpsDtoForApi>
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
