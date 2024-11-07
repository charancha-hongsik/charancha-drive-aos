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
    @ColumnInfo(name="bluetooth_name") var bluetooth_name: String?,
    @ColumnInfo(name="start_address") var start_address: String?,
    @ColumnInfo(name="end_address") var end_address: String?,
    @ColumnInfo(name="end_address_detail") var end_address_detail: String?,
    @ColumnInfo(name="gpses") var gpses: List<EachGpsDtoForApp>
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
