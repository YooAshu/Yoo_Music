package com.example.yoomusic.data

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class LrcLibResponse(
    val id: Int,
    val name: String,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val duration: Float,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?
)
