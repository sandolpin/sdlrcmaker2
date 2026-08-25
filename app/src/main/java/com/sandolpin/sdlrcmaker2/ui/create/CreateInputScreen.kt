package com.sandolpin.sdlrcmaker2.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.FileFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInputScreen(
    viewModel: CreateInputViewModel,
    onBack: () -> Unit,
    onProceedToEdit: () -> Unit,
) {
    val title by viewModel.title.collectAsState()
    val artist by viewModel.artist.collectAsState()
    val lyricsText by viewModel.lyricsText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${viewModel.format.label}を作成する") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
        ) {
            Text("1.タイトル名を入力", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text(
                "ファイル名のタイトルに使われます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            if (viewModel.format == FileFormat.SDLRC) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("2.アーティスト名を入力", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = artist,
                    onValueChange = viewModel::onArtistChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    "sdlrcに埋め込みます。同名ファイルの識別に使います",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = viewModel::pasteFromNowPlaying,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ContentPaste, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("再生中の曲のデータを貼り付ける")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (viewModel.format == FileFormat.SDLRC) "3.歌詞を貼り付け" else "2.歌詞を貼り付け",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lyricsText,
                onValueChange = viewModel::onLyricsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onProceedToEdit,
                enabled = viewModel.canProceed(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("編集にすすむ")
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
