package com.example.keeptrack.data.local

import android.content.Context

object DatabaseProvider {

    @Volatile
    private var database: KeepTrackDatabase? = null

    fun getDatabase(context: Context): KeepTrackDatabase {
        return database ?: synchronized(this) {
            database ?: KeepTrackDatabase
                .getDatabase(context.applicationContext)
                .also {
                    database = it
                }
        }
    }
}