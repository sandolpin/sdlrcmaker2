package com.sandolpin.sdlrcmaker2.ui.removestamp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveTimestampScreen(
    viewModel: RemoveTimestampViewModel,
    onBack: () -> Unit,
    onSaveAsText: () -> Unit,
) {
    val loadedFile by viewModel.loadedFile.collectAsState()
    val plainText by viewModel.plainText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("タイムスタンプを削除") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "戻る")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "プレーンテキストとしてコピーできます。\nテキストファイルとして保存も可能です。",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            HorizontalDivider()

            if (loadedFile == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "sdlrc/lrcファイルを選択してください",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                Text(
                    text = plainText,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (loadedFile != null) {
                Button(
                    onClick = onSaveAsText,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("テキストファイルで保存")
                }
            }
        }
    }
}
