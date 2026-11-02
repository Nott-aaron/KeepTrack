package com.example.keeptrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.keeptrack.data.model.RecordEntity

@Database(
    entities = [RecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KeepTrackDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {

        @Volatile
        private var INSTANCE: KeepTrackDatabase? = null

        fun getDatabase(context: Context): KeepTrackDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeepTrackDatabase::class.java,
                    "keeptrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}