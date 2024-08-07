package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milelog.room.dto.EachGpsDtoForApp

/**
 * 성공 시 저장되는 데이터
 */
@Entity(tableName = "drive")
data class DriveForApp(
    @ColumnInfo(name="tracking_Id") var tracking_id: String, // APP쪽에서의 임의 tracking_id
    @ColumnInfo(name="gpses") var gpses: List<EachGpsDtoForApp>
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
