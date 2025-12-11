package com.example.yoomusic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LyricsCache::class], version = 1)
abstract class LyricsDatabase : RoomDatabase() {
    abstract fun lyricsCacheDao(): LyricsCacheDao

    companion object {
        @Volatile
        private var INSTANCE: LyricsDatabase? = null

        fun getDatabase(context: Context): LyricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LyricsDatabase::class.java,
                    "lyrics_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
