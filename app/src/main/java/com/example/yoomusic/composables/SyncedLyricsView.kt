package com.example.yoomusic.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yoomusic.data.LyricLine
@Composable
fun SyncedLyricsView(
    lyrics: List<LyricLine>,
    currentIndex: Int
) {
    // List state for auto scroll
    val listState = rememberLazyListState()

    // Scroll so current line stays near center (offset = half screen)
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(
                currentIndex,
                scrollOffset = -200  // adjust for perfect centering
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = 100.dp,
                bottom = 200.dp
            )
        ) {

            itemsIndexed(lyrics) { index, line ->

                // How far this line is from the current highlighted line
                val distance = kotlin.math.abs(index - currentIndex)

                // Scale decreases as distance increases
                val scale = when (distance) {
                    0 -> 1.00f          // Current line: largest
                    1 -> 0.80f
                    2 -> 0.75f
                    3 -> 0.70f
                    else -> 0.70f       // Far lines: small
                }

                // Alpha/Fade effect
                val alpha = when (distance) {
                    0 -> 1.0f           // Current line fully visible
                    1 -> 0.7f
                    2 -> 0.55f
                    3 -> 0.35f
                    else -> 0.20f       // Far away lines almost faded
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line.text,
                        fontSize = 22.sp,
                        fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
