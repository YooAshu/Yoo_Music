package com.example.yoomusic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_cache")
data class LyricsCache(
    @PrimaryKey
    val musicId: String,
    val lyricsContent: String,
    val timestamp: Long = System.currentTimeMillis()
)
