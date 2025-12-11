package com.example.yoomusic.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun LyricsPlaceholderScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),   // ← FIXED
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()               // ← FIXED
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lyrics will appear here",
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}

