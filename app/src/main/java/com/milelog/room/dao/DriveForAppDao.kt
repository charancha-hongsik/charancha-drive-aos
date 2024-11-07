package com.milelog.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.milelog.room.entity.DriveForApp

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
    @get:Query("SELECT * FROM drive ORDER BY tracking_Id DESC LIMIT 10")
    val allDriveForApp: List<DriveForApp>?

    /**
     * 모든 Drving 값 가져오기
     */
    @get:Query("SELECT * FROM drive ORDER BY tracking_Id DESC LIMIT 10")
    val allDriveLimit5ForApp: List<DriveForApp>?

    @Query("SELECT * FROM drive WHERE tracking_id = :trackingId")
    fun getDriveByTrackingId(trackingId: String): DriveForApp?

    // tracking_id를 업데이트하는 메서드
    @Query("UPDATE drive SET tracking_Id = :newTrackingId WHERE tracking_id = :id")
    fun updateTrackingId(id: String, newTrackingId: String)

    // startAddress를 업데이트하는 메서드
    @Query("UPDATE drive SET start_address = :startAddress WHERE tracking_id = :id")
    fun updateStartAddress(id: String, startAddress: String)

    // endAddress를 업데이트하는 메서드
    @Query("UPDATE drive SET end_address = :endAddress WHERE tracking_id = :id")
    fun updateEndAddress(id: String, endAddress: String)

    // endAddressDetail를 업데이트하는 메서드
    @Query("UPDATE drive SET end_address_detail = :endAddressDetail WHERE tracking_id = :id")
    fun updateEndAddressDetail(id: String, endAddressDetail: String)


}