package com.milelog.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.milelog.room.entity.DetectUserEntity

@Dao
interface DetectUserDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(detectUserEntity: DetectUserEntity?)

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM detectUserEntity ORDER BY idx DESC")
    val detectUserEntity: MutableList<DetectUserEntity>?

}