package com.milelog.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.milelog.room.Converters
import com.milelog.room.dao.DriveForAppDao
import com.milelog.room.dao.DriveForApiDao
import com.milelog.room.entity.DriveForApi
import com.milelog.room.entity.DriveForApp

@Database(entities = [DriveForApp::class, DriveForApi::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveForAppDao(): DriveForAppDao
    abstract fun driveForApiDao(): DriveForApiDao

    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database")
                    .allowMainThreadQueries()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}