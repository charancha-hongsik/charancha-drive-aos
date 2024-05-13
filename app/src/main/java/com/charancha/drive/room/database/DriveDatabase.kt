package com.charancha.drive.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.charancha.drive.room.dao.DriveDao
import com.charancha.drive.room.entity.Drive

@Database(entities = [Drive::class], version = 2, exportSchema = false)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveDao(): DriveDao

    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_database").allowMainThreadQueries().addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새로운 테이블 생성
                database.execSQL("CREATE TABLE IF NOT EXISTS drive2 (" +
                        "tracking_id TEXT NOT NULL PRIMARY KEY," +
                        "timeStamp INTEGER NOT NULL," +
                        "verification TEXT NOT NULL," +
                        "distance REAL NOT NULL," +
                        "time INTEGER NOT NULL," +
                        "sudden_deceleration INTEGER NOT NULL," +
                        "sudden_stop INTEGER NOT NULL," +
                        "sudden_acceleration INTEGER NOT NULL," +
                        "sudden_start INTEGER NOT NULL," +
                        "high_speed_driving REAL NOT NULL," +
                        "low_speed_driving REAL NOT NULL," +
                        "constant_speed_driving REAL NOT NULL," +
                        "jsonData TEXT NOT NULL" +
                        ")")

                // 이전 테이블의 데이터를 새로운 테이블로 이동
                database.execSQL("INSERT INTO drive2 (tracking_id, timeStamp, verification, distance, time, sudden_deceleration, sudden_stop, sudden_acceleration, sudden_start, high_speed_driving, low_speed_driving, constant_speed_driving, jsonData) SELECT tracking_id, timeStamp, rank, distance, time, rapid1, rapid2, 0, 0, 0.0, 0.0, 0.0, jsonData FROM drive")

                // 이전 테이블 삭제
                database.execSQL("DROP TABLE drive")
            }
        }
    }
}