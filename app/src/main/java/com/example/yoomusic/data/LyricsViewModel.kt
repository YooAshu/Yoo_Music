package com.example.yoomusic.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoomusic.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LyricsViewModel(
    private val repository: LyricsRepository = LyricsRepository()
) : ViewModel() {

    private val _songPosition = MutableStateFlow(MusicPlayer.songPosition)
    val songPosition: StateFlow<Int> = _songPosition.asStateFlow()


    private val _lyricsState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val lyricsState: StateFlow<LyricsUiState> = _lyricsState.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    /**
     * Fetch lyrics for a given song title.
     * This is triggered when user clicks "Fetch Lyrics".
     */
    fun fetchLyrics(songTitle: String) {
        viewModelScope.launch {
            _lyricsState.value = LyricsUiState.Loading

            repository.searchLyrics(songTitle)
                .onSuccess { result ->

                    if (result == null) {
                        _lyricsState.value = LyricsUiState.NotFound
                        return@launch
                    }

                    // If synced lyrics exist → parse them
                    if (!result.syncedLyrics.isNullOrBlank()) {
                        val parsed = LrcParser.parse(result.syncedLyrics)
                        _lyricsState.value = LyricsUiState.Success(parsed)
                        return@launch
                    }

                    // If only plain lyrics exist → split into lines
                    if (!result.plainLyrics.isNullOrBlank()) {
                        val lines = result.plainLyrics
                            .lines()
                            .filter { it.isNotBlank() }
                            .map { LyricLine(timestampMs = 0L, text = it) }

                        _lyricsState.value = LyricsUiState.Success(lines)
                        return@launch
                    }

                    _lyricsState.value = LyricsUiState.NotFound
                }
                .onFailure { error ->
                    _lyricsState.value = LyricsUiState.Error(
                        message = error.message ?: "Failed to fetch lyrics"
                    )
                }
        }
    }

    /**
     * Sync the current highlighted lyric with the media player position.
     */
    fun updateCurrentPosition(currentMs: Long) {
        val state = _lyricsState.value
        if (state is LyricsUiState.Success) {
            val idx = state.lyrics.indexOfLast { it.timestampMs <= currentMs }
            if (idx != -1) _currentLineIndex.value = idx
        }
    }

    fun setLyricsStateToIdle() {
        _lyricsState.value = LyricsUiState.Idle
    }

    fun updateSongPosition(position: Int) {
        _songPosition.value = position
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
