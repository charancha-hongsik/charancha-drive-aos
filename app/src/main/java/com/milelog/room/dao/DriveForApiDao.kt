package com.milelog.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.milelog.room.entity.DriveForApi

@Dao
interface DriveForApiDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(drive: DriveForApi?)

    @Query("DELETE FROM driveForApi")
    fun deleteAll()

    @Query("DELETE FROM driveForApi WHERE tracking_id = :trackingId")
    fun deleteByTrackingId(trackingId: String)

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM driveForApi ORDER BY startTimeStamp ASC LIMIT 5")
    val allDriveLimit5: List<DriveForApi>?


    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM driveForApi ORDER BY startTimeStamp DESC")
    val allDrive: List<DriveForApi>?

}