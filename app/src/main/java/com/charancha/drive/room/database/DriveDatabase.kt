package com.charancha.drive.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.charancha.drive.room.dao.DriveDao
import com.charancha.drive.room.entity.Drive
import java.util.concurrent.Executors

@Database(entities = [Drive::class], version = 1, exportSchema = false)
abstract class DriveDatabase : RoomDatabase() {
    abstract fun driveDao(): DriveDao?

    companion object {
        @Volatile
        private var INSTANCE: DriveDatabase? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        fun getDatabase(context: Context): DriveDatabase? {
            if (INSTANCE == null) {
                synchronized(DriveDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            DriveDatabase::class.java, "drive_database"
                        ).allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}