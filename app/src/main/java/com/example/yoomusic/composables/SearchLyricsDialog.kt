package com.example.yoomusic.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yoomusic.data.LrcLibResponse
import com.example.yoomusic.data.LyricsViewModel

@Composable
fun SearchLyricsDialog(
    viewModel: LyricsViewModel,
    onDismiss: () -> Unit,
    title:String="",
    onSelect: (LrcLibResponse) -> Unit
) {
    var searchQuery by remember { mutableStateOf(title) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    LaunchedEffect(title) {
        viewModel.clearSearchResults()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.8f)
                .glassCard()
                .background(Color(0xFF2E2E2E))
                .padding(16.dp)
        ) {
            Text(
                text = "Search Lyrics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Enter song title", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2E2E2E),
                    unfocusedContainerColor = Color(0xFF2E2E2E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF9D00FF)
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchLyricsManually(searchQuery)
                        }
                    }
                ),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchLyricsManually(searchQuery)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF505050),
                    contentColor = Color.White)
                ,
                enabled = searchQuery.isNotBlank() && !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isSearching) "Searching..." else "Search" , color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            if (searchResults.isNotEmpty()) {
                Text(
                    text = "Results (${searchResults.size})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = { onSelect(result) }
                        )
                    }
                }
            } else if (searchQuery.isNotBlank() && !isSearching && searchResults.isEmpty()) {
                Text(
                    text = "No results found",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: LrcLibResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E2E2E), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.trackName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.artistName,
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "${(result.duration / 60).toInt()}:${(result.duration.toInt() % 60).toString().padStart(2, '0')}",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.width(40.dp)
        )
    }
}

fun Modifier.glassCard(corner: Int = 30): Modifier = this
    .clip(RoundedCornerShape(corner.dp))
    .background(Color.White.copy(alpha = 0.08f))
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(corner.dp)
    )