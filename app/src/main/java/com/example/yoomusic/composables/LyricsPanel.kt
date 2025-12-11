package com.example.yoomusic.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yoomusic.MusicPlayer
import com.example.yoomusic.MusicPlayer.Companion.musicListPA
import com.example.yoomusic.data.LyricsUiState
import com.example.yoomusic.data.LyricsViewModel
import androidx.compose.foundation.background

@Composable
fun LyricsPanel(
    viewModel: LyricsViewModel,
) {
    val state by viewModel.lyricsState.collectAsState()
    val songPosition by viewModel.songPosition.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }

    val title = if (songPosition < musicListPA.size) {
        musicListPA[songPosition].title
    } else {
        "Unknown"
    }

    val musicId = if (songPosition < musicListPA.size) {
        musicListPA[songPosition].id
    } else {
        ""
    }

    LaunchedEffect(songPosition) {
        viewModel.loadLyricsFromCache(musicId = musicId)
        viewModel.setLyricsStateToIdle()
        viewModel.updateCurrentLineIndex(0)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit lyrics",
                    tint = Color(0xFF9D00FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            LyricsUiState.Idle -> {
                Text("No lyrics found", color = Color.White)
                Button(
                    onClick = { viewModel.fetchLyrics(title, musicId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D00FF),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0x90C29AFF))
                ) {
                    Text("Fetch lyrics")
                }
            }

            LyricsUiState.Loading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text("Fetching lyrics...", color = Color.White)
                }
            }

            is LyricsUiState.Success -> {
                val currentIndex by viewModel.currentLineIndex.collectAsState()
                SyncedLyricsView(
                    lyrics = s.lyrics,
                    currentIndex = currentIndex
                )
            }

            LyricsUiState.NotFound -> {
                Text(
                    text = "No lyrics found for this song.",
                    color = Color.LightGray
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchLyrics(title, musicId) }) {
                    Text("Try again")
                }
            }

            is LyricsUiState.Error -> {
                Text(
                    text = "Error: ${s.message}",
                    color = Color.Red
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchLyrics(title, musicId) }) {
                    Text("Retry")
                }
            }
        }
    }

    if (showSearchDialog) {
        SearchLyricsDialog(
            viewModel = viewModel,
            title = title,
            onDismiss = { showSearchDialog = false },
            onSelect = { selectedResult ->
                viewModel.selectAndSaveLyrics(selectedResult, musicId)
                showSearchDialog = false
            }
        )
    }
}
