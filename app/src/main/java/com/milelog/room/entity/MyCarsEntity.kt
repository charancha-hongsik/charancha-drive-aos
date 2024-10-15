package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "myCars")
data class MyCarsEntity(
    @ColumnInfo(name="id") var id: String,
    @ColumnInfo(name="name") var name: String,
    @ColumnInfo(name="bluetooth_mac_address") var bluetooth_mac_address: String
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
