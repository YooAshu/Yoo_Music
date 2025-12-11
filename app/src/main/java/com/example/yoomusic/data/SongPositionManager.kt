package com.example.yoomusic.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SongPositionManager {
    private val _songPosition = MutableStateFlow(0)
    val songPosition: StateFlow<Int> = _songPosition.asStateFlow()

    fun updateSongPosition(position: Int) {
        _songPosition.value = position
    }
}
