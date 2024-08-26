package com.milelog.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.milelog.room.entity.AlarmEntity
import com.milelog.room.entity.DriveForApi

@Dao
interface AlarmDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(alarm: AlarmEntity?)

    @Query("DELETE FROM alarmEntity")
    fun deleteAll()

    /**
     * offset 갯수의 Alarm은 건더뛰고 30개의 Alarm 가져오기
     * ex) offset 0 -> 1~30번째의 Alarm을 가져옴.
     */
    @Query("SELECT * FROM alarmEntity WHERE user_id = :userId ORDER BY idx DESC LIMIT 30 OFFSET :offset ")
    fun getAlarmLimit30(userId: String, offset:Int):MutableList<AlarmEntity>?

    @Query("SELECT COUNT(*) FROM alarmEntity WHERE user_id = :userId ORDER BY idx DESC")
    fun getAlarmCount(userId: String): Int

    @Query("UPDATE alarmEntity SET isRequired = :isRequired WHERE idx = :idx")
    fun updateIsRequired(idx: Long, isRequired: Boolean = false): Int

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM alarmEntity ORDER BY idx DESC")
    val allAlarm: MutableList<AlarmEntity>?

}