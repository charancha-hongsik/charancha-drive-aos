package com.milelog.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.milelog.activity.LoadCarMoreInfoActivity.Companion.PERSONAL

@Entity(tableName = "myCars")
data class MyCarsEntity(
    @ColumnInfo(name="id") var id: String?, // Car ID
    @ColumnInfo(name="name") var name: String?, // Car Name
    @ColumnInfo(name="number") var number: String?, // Car License Number
    @ColumnInfo(name="bluetooth_mac_address") var bluetooth_mac_address: String?,
    @ColumnInfo(name="bluetooth_name") var bluetooth_name: String?,
    @ColumnInfo(name="isActive") var isActive: Boolean?=true,
    @ColumnInfo(name="type") var type: String? = null,
){
    @PrimaryKey(autoGenerate = true)
    var idx: Long = 0
}
