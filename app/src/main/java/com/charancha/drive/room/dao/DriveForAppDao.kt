package com.charancha.drive.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charancha.drive.room.entity.DriveForApp

@Dao
interface DriveForAppDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(driveForApp: DriveForApp?)

    @Query("DELETE FROM drive")
    fun deleteAll()


    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM drive ORDER BY startTimestamp DESC LIMIT 10")
    val allDriveForApp: List<DriveForApp>?

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM drive ORDER BY startTimestamp DESC LIMIT 10")
    val allDriveLimit5ForApp: List<DriveForApp>?

    @Query("SELECT * FROM drive WHERE tracking_id = :trackingId")
    fun getDriveByTrackingId(trackingId: String): DriveForApp?

}