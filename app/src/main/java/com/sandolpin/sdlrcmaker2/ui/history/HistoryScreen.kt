package com.sandolpin.sdlrcmaker2.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onResume: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
) {
    val entries by viewModel.entries.collectAsState()
    val formatter = remember { SimpleDateFormat("yyyy/M/d HH:mm更新", Locale.JAPAN) }

    // 長押しでの誤削除を防ぐため、即削除はせずいったん確認ダイアログを挟む
    var entryPendingDelete by remember { mutableStateOf<HistoryEntry?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("作成履歴") }) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "作業を再開しましょう",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "まだ作成履歴がありません",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        HistoryCard(
                            entry = entry,
                            dateLabel = formatter.format(Date(entry.updatedAtMs)),
                            onClick = { onResume(entry) },
                            onLongClick = { entryPendingDelete = entry },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    val target = entryPendingDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("削除しますか?") },
            text = { Text("「${target.title}」を作成履歴から削除します。この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target)
                    entryPendingDelete = null
                }) { Text("削除する") }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) { Text("キャンセル") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    dateLabel: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        AssistChip(
            onClick = {},
            label = { Text(entry.format.label) },
            leadingIcon = { Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) },
        )
    }
}