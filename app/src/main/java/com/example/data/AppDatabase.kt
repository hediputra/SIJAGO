package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Citizen::class,
        LetterRequest::class,
        NewsItem::class,
        ProjectItem::class,
        ReportItem::class,
        AttendanceRecord::class,
        UmkmItem::class,
        VillageEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun letterRequestDao(): LetterRequestDao
    abstract fun newsDao(): NewsDao
    abstract fun projectDao(): ProjectDao
    abstract fun reportDao(): ReportDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun umkmDao(): UmkmDao
    abstract fun villageEventDao(): VillageEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sijago_desaku_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
