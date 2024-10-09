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

@Database(entities = [DriveForApp::class, DriveForApi::class, AlarmEntity::class, DetectUserEntity::class], version = 3, exportSchema = false)
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

    }
}