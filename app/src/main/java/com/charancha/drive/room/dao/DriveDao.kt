package com.charancha.drive.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charancha.drive.room.entity.Drive

@Dao
interface DriveDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(drive: Drive?)

    @Query("DELETE FROM drive")
    fun deleteAll()

    @get:Query("SELECT * FROM drive ORDER BY timeStamp DESC")
    val allDrive: List<Drive?>?

    /**
     * 특정 날짜의 Drive 값 모두 가져오기
     */
    @Query("SELECT * FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1")
    fun allDriveBetween(minTimeStamp: Long, maxTimeStamp1: Long): List<Drive>
}