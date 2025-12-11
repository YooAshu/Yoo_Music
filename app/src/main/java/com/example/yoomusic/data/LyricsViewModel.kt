package com.example.yoomusic.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoomusic.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LyricsViewModel(application: Application) : AndroidViewModel(application) {
    private val lyricsCacheDao = LyricsDatabase.getDatabase(application).lyricsCacheDao()

    private val _lyricsState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val lyricsState: StateFlow<LyricsUiState> = _lyricsState.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    private val _songPosition = MutableStateFlow(0)
    val songPosition: StateFlow<Int> = _songPosition.asStateFlow()


    init {
        // Observe SongPositionManager changes
        viewModelScope.launch {
            SongPositionManager.songPosition.collect { position ->
                updateSongPosition(position)
            }
        }
    }


    fun updateSongPosition(position: Int) {
        _songPosition.value = position
//        loadLyricsFromCache(musicId = MusicPlayer.musicListPA[position].id)
    }

    fun setLyricsStateToIdle() {
        _lyricsState.value = LyricsUiState.Idle
    }

    fun loadLyricsFromCache(musicId: String) {
        Log.d("loadLyricsFromCache", "Loading lyrics for musicId: $musicId")
        viewModelScope.launch {
            try {
                val cachedLyrics = lyricsCacheDao.getLyricsByMusicId(musicId)
                if (cachedLyrics != null) {
                    val parsedLyrics = LrcParser.parse(cachedLyrics)
                    _lyricsState.value = LyricsUiState.Success(parsedLyrics)
                } else {
                    _lyricsState.value = LyricsUiState.Idle
                }
            } catch (e: Exception) {
                _lyricsState.value = LyricsUiState.Idle
            }
        }
    }

    fun fetchLyrics(title: String, musicId: String) {
        viewModelScope.launch {
            _lyricsState.value = LyricsUiState.Loading

            // Check cache first
            val cachedLyrics = lyricsCacheDao.getLyricsByMusicId(musicId)
            if (cachedLyrics != null) {
                // Parse cached string to List<LyricLine>
                val parsedLyrics = LrcParser.parse(cachedLyrics)
                _lyricsState.value = LyricsUiState.Success(parsedLyrics)
                return@launch
            }

            // If not in cache, fetch from API
            try {
                val result = LyricsRepository().searchLyrics(title)
                result.onSuccess { lyricsResult ->
                    if (lyricsResult == null) {
                        _lyricsState.value = LyricsUiState.NotFound
                        return@launch
                    }

                    // Parse synced lyrics if available
                    if (!lyricsResult.syncedLyrics.isNullOrBlank()) {
                        val parsed = LrcParser.parse(lyricsResult.syncedLyrics)
                        // Save to cache as string
                        lyricsCacheDao.insertLyrics(
                            LyricsCache(
                                musicId = musicId,
                                lyricsContent = lyricsResult.syncedLyrics
                            )
                        )
                        _lyricsState.value = LyricsUiState.Success(parsed)
                        return@launch
                    }

                    // If only plain lyrics exist → split into lines
                    if (!lyricsResult.plainLyrics.isNullOrBlank()) {
                        val lines = lyricsResult.plainLyrics
                            .lines()
                            .filter { it.isNotBlank() }
                            .map { LyricLine(timestampMs = 0L, text = it) }

                        // Save plain lyrics to cache
                        lyricsCacheDao.insertLyrics(
                            LyricsCache(
                                musicId = musicId,
                                lyricsContent = lyricsResult.plainLyrics
                            )
                        )
                        _lyricsState.value = LyricsUiState.Success(lines)
                        return@launch
                    }

                    _lyricsState.value = LyricsUiState.NotFound
                }
                result.onFailure { error ->
                    _lyricsState.value = LyricsUiState.Error(
                        message = error.message ?: "Failed to fetch lyrics"
                    )
                }
            } catch (e: Exception) {
                _lyricsState.value = LyricsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCurrentPosition(currentMs: Long) {
        val state = _lyricsState.value
        if (state is LyricsUiState.Success) {
            val idx = state.lyrics.indexOfLast { it.timestampMs <= currentMs }
            if (idx != -1) _currentLineIndex.value = idx
        }
    }

    fun updateCurrentLineIndex(index: Int) {
        _currentLineIndex.value = index
    }
}

sealed class LyricsUiState {
    object Idle : LyricsUiState()
    object Loading : LyricsUiState()
    object NotFound : LyricsUiState()
    data class Success(val lyrics: List<LyricLine>) : LyricsUiState()
    data class Error(val message: String) : LyricsUiState()
}

object LyricsSearchHelper {
    fun cleanSongTitle(title: String): String {
        return title
            .replace("""\.(mp3|m4a|flac|wav|aac|ogg|wma)$""".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("""(\d+)(kbps|Kbps|KBPS)""".toRegex(), "")
            .replace("""\s*(Full |Official |Lyric |Music )?(Video|Audio)\s*""".toRegex(RegexOption.IGNORE_CASE), " ")
            .replace("""\[.*?]""".toRegex(), "")
            .replace("""\(.*?\)""".toRegex(), "")
            .replace("""[|｜].*""".toRegex(), "")
            .replace("""\s+""".toRegex(), " ")
            .trim()
    }
}
