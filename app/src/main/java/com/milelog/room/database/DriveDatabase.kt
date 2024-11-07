package com.milelog.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.milelog.room.Converters
import com.milelog.room.dao.AlarmDao
import com.milelog.room.dao.DetectUserDao
import com.milelog.room.dao.DriveForAppDao
import com.milelog.room.dao.DriveForApiDao
import com.milelog.room.entity.AlarmEntity
import com.milelog.room.entity.DetectUserEntity
import com.milelog.room.entity.DriveForApi
import com.milelog.room.entity.DriveForApp

@Database(entities = [DriveForApp::class, DriveForApi::class, AlarmEntity::class, DetectUserEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveForAppDao(): DriveForAppDao
    abstract fun driveForApiDao(): DriveForApiDao
    abstract fun alarmDao(): AlarmDao
    abstract fun detectUserDao(): DetectUserDao



    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database")
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .allowMainThreadQueries()
                    .build()
                    .also { Instance = it }
            }
        }


        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새로운 alarmEntity 테이블 생성
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `alarmEntity` (
                `idx` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `user_id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `body` TEXT NOT NULL, 
                `deepLink` TEXT NOT NULL, 
                `timestamp` TEXT NOT NULL, 
                `imageUrl` TEXT NOT NULL, 
                `type` TEXT NOT NULL, 
                `isRequired` INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새로운 alarmEntity 테이블 생성
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `detectUserEntity` (
                `idx` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `user_id` TEXT NOT NULL, 
                `verification` TEXT NOT NULL, 
                `start_stop` TEXT NOT NULL, 
                `timestamp` TEXT NOT NULL, 
                `sensor_state` INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // driveForApi 테이블의 임시 테이블 생성
                database.execSQL("""
            CREATE TABLE driveForApi_temp (
                idx INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tracking_Id TEXT NOT NULL,
                userCarId TEXT,
                startTimestamp INTEGER NOT NULL,
                endTimestamp INTEGER NOT NULL,
                verification TEXT NOT NULL,
                gpses TEXT NOT NULL,
                bluetooth_name TEXT
            )
        """)

                // driveForApi 기존 데이터 복사
                database.execSQL("""
            INSERT INTO driveForApi_temp (idx, tracking_Id, userCarId, startTimestamp, endTimestamp, verification, gpses, bluetooth_name)
            SELECT idx, tracking_Id, userCarId, startTimestamp, endTimestamp, verification, gpses, NULL
            FROM driveForApi
        """)

                // 기존 driveForApi 테이블 삭제 및 이름 변경
                database.execSQL("DROP TABLE driveForApi")
                database.execSQL("ALTER TABLE driveForApi_temp RENAME TO driveForApi")

                // drive 테이블의 임시 테이블 생성
                database.execSQL("""
            CREATE TABLE drive_temp (
                idx INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tracking_Id TEXT NOT NULL,
                bluetooth_name TEXT,
                gpses TEXT NOT NULL
            )
        """)

                // drive 기존 데이터 복사
                database.execSQL("""
            INSERT INTO drive_temp (idx, tracking_Id, gpses, bluetooth_name)
            SELECT idx, tracking_Id, gpses, NULL
            FROM drive
        """)

                // 기존 drive 테이블 삭제 및 이름 변경
                database.execSQL("DROP TABLE drive")
                database.execSQL("ALTER TABLE drive_temp RENAME TO drive")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 임시 테이블 생성 (gpses를 TEXT 타입으로)
                database.execSQL("""
            CREATE TABLE drive_temp (
                idx INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tracking_Id TEXT NOT NULL,
                bluetooth_name TEXT,
                start_address TEXT,
                end_address TEXT,
                gpses TEXT NOT NULL
            )
        """)

                // 기존 테이블의 데이터를 임시 테이블로 복사
                database.execSQL("""
            INSERT INTO drive_temp (idx, tracking_Id, bluetooth_name, start_address, end_address, gpses)
            SELECT idx, tracking_Id, bluetooth_name, NULL, NULL, gpses
            FROM drive
        """)

                // 기존 테이블 삭제
                database.execSQL("DROP TABLE drive")

                // 임시 테이블 이름을 원래 테이블 이름으로 변경
                database.execSQL("ALTER TABLE drive_temp RENAME TO drive")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새로운 컬럼을 추가합니다.
                database.execSQL("ALTER TABLE drive ADD COLUMN end_address_detail TEXT")
            }
        }
    }
}