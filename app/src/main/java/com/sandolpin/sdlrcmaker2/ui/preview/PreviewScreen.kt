package com.sandolpin.sdlrcmaker2.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.ui.components.MediaPlayerBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel,
    onBack: () -> Unit,
    onExport: () -> Unit,
) {
    val playbackInfo by viewModel.playbackInfo.collectAsState()
    val activeIndex by viewModel.activeLineIndex.collectAsState()
    val lines = viewModel.lyricFile.lines

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("確認・出力") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "戻る")
                    }
                },
                actions = { TextButton(onClick = onExport) { Text("出力へ") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "歌詞が時間通りに正しく表示されているか確認してください。",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(lines) { line ->
                    val index = lines.indexOf(line)
                    val isActive = index == activeIndex
                    Text(
                        text = line.text,
                        style = if (isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = if (line.isRightAlign) androidx.compose.ui.text.style.TextAlign.End
                        else androidx.compose.ui.text.style.TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            MediaPlayerBar(
                playbackInfo = playbackInfo,
                onPlayPause = viewModel::onPlayPause,
                onSkip = viewModel::onSkip,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}