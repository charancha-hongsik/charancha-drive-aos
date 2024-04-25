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
    fun insert(recentCarEntity: Drive?)

    @Query("DELETE FROM Drive")
    fun deleteAll()

    @get:Query("SELECT * FROM Drive ORDER BY timeStamp DESC")
    val allDrive: List<Drive?>?
}