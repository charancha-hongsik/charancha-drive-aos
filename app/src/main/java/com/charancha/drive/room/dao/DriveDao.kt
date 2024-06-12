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


    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM drive ORDER BY timeStamp DESC LIMIT 5")
    val allDrive: List<Drive>?

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM drive ORDER BY timeStamp DESC LIMIT 5")
    val allDriveLimit5: List<Drive>?

    @Query("SELECT * FROM drive WHERE tracking_id = :trackingId")
    fun getDriveByTrackingId(trackingId: String): Drive?

    /**
     * 특정 날짜 사이의 Drive 값 모두 가져오기
     */
    @Query("SELECT * FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun allDriveBetween(minTimeStamp: Long, maxTimeStamp1: Long): List<Drive>


    /**
     * 특정 날짜 사이의 거리 리스트 모두 가져오기
     */
    @Query("SELECT distance_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllDistanceArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 급감속 리스트 모두 가져오기
     */
    @Query("SELECT sudden_deceleration_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllSuddenDecelerationArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 급정지 리스트 모두 가져오기
     */
    @Query("SELECT sudden_stop_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllSuddenStopArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 급가속 리스트 모두 가져오기
     */
    @Query("SELECT sudden_acceleration_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllSuddenAccelerationArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 급출발 리스트 모두 가져오기
     */
    @Query("SELECT sudden_start_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllSuddenStartArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 고속 주행거리 리스트 모두 가져오기
     */
    @Query("SELECT high_speed_driving_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllHighSpeedDrivingArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 저속 주행거리 리스트 모두 가져오기
     */
    @Query("SELECT low_speed_driving_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllLowSpeedDrivingArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 항속 주행거리 리스트 모두 가져오기
     */
    @Query("SELECT constant_speed_driving_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllConstantSpeedDrivingArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 harsh drving 리스트 모두 가져오기
     */
    @Query("SELECT harsh_driving_array FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllHarshDrivingArrays(minTimeStamp: Long, maxTimeStamp1: Long): List<String>

    /**
     * 특정 날짜 사이의 급가감속 값 모두 가져오기
     */
    @Query("SELECT sum_sudden_deceleration_speed FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getSumSuddenDecelerationSpeed(minTimeStamp: Long, maxTimeStamp1: Long): Float

    /**
     * 특정 날짜 사이의 rawData 모두 가져오기
     */
    @Query("SELECT jsonData FROM drive WHERE timeStamp BETWEEN :minTimeStamp AND :maxTimeStamp1 ORDER BY timeStamp DESC")
    fun getAllJsonDataArrays(minTimeStamp: Long, maxTimeStamp1: Long): Float

}