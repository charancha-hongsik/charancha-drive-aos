package com.charancha.drive.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.charancha.drive.room.dao.DriveDao
import com.charancha.drive.room.entity.Drive

@Database(entities = [Drive::class], version = 1, exportSchema = false)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveDao(): DriveDao

    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}