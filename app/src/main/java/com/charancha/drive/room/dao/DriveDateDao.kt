package com.charancha.drive.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charancha.drive.room.entity.Drive
import com.charancha.drive.room.entity.DriveDate

@Dao
interface DriveDateDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(driveDate: DriveDate?)

    @Query("DELETE FROM drive_date")
    fun deleteAll()

    @get:Query("SELECT * FROM drive_date ORDER BY date DESC")
    val allDriveDate: List<DriveDate?>?
}