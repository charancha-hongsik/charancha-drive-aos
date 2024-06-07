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

@Database(entities = [Drive::class, DriveForApi::class], version = 2, exportSchema = false)
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
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database").allowMainThreadQueries()
                    .build()
                    .also { Instance = it }
            }
        }

    }
}