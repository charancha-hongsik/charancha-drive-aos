package com.charancha.drive.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive_date")
data class DriveDate(
    @field:PrimaryKey var date: Int
)