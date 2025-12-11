package com.example.yoomusic.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsCacheDao {
    @Query("SELECT lyricsContent FROM lyrics_cache WHERE musicId = :musicId")
    suspend fun getLyricsByMusicId(musicId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyricsCache: LyricsCache)

    @Query("DELETE FROM lyrics_cache WHERE musicId = :musicId")
    suspend fun deleteLyrics(musicId: String)

    @Query("DELETE FROM lyrics_cache")
    suspend fun deleteAllLyrics()
}
