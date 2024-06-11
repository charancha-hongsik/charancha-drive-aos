package com.charancha.drive.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.charancha.drive.room.Converters
import com.charancha.drive.room.dao.DriveDao
import com.charancha.drive.room.dao.DriveForApiDao
import com.charancha.drive.room.entity.Drive
import com.charancha.drive.room.entity.DriveForApi

@Database(entities = [Drive::class, DriveForApi::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveDao(): DriveDao
    abstract fun driveForApiDao(): DriveForApiDao

    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `driveForApi` (
                        `tracking_id` TEXT NOT NULL,
                        `manufacturer` TEXT NOT NULL,
                        `version` TEXT NOT NULL,
                        `deviceModel` TEXT NOT NULL,
                        `deviceUuid` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `startTimeStamp` INTEGER NOT NULL,
                        `endTimestamp` INTEGER NOT NULL,
                        `verification` TEXT NOT NULL,
                        `gpses` TEXT NOT NULL,
                        PRIMARY KEY(`tracking_id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE driveForApi ADD COLUMN automobile INTEGER NOT NULL DEFAULT 1")
            }
        }

    }
}