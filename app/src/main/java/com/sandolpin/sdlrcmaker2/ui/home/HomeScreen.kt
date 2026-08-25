package com.sandolpin.sdlrcmaker2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.FileFormat

@Composable
fun HomeScreen(
    onCreate: (FileFormat) -> Unit,
    onImport: () -> Unit,
    onRemoveTimestamp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("sdlrcメーカー", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "作る歌詞ファイルを選んでください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(20.dp))

        HomeMenuCard(
            title = "sdlrc",
            description = "通常のlrcに加えてさまざまな機能を利用できます。利用できるアプリは" +
                "「さんとぴメディアコントロール」のみです。",
            icon = Icons.Filled.MusicNote,
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = { onCreate(FileFormat.SDLRC) },
        )
        Spacer(modifier = Modifier.height(14.dp))
        HomeMenuCard(
            title = "lrc",
            description = "さまざまなアプリと互換性があります。内部が単純なので簡単につくれます。",
            icon = Icons.Filled.MusicNote,
            containerColor = MaterialTheme.colorScheme.secondary,
            onClick = { onCreate(FileFormat.LRC) },
        )
        Spacer(modifier = Modifier.height(14.dp))
        HomeMenuCard(
            title = "ファイルをインポート",
            description = "sdlrcまたはlrcをインポートして編集します",
            icon = Icons.Filled.FileUpload,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            onClick = onImport,
        )
        Spacer(modifier = Modifier.height(14.dp))
        HomeMenuCard(
            title = "タイムスタンプを削除",
            description = "時間が記録された歌詞からタイムスタンプのみ削除",
            icon = Icons.Filled.Delete,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            onClick = onRemoveTimestamp,
        )
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val isNeutral = containerColor == MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isNeutral) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.85f))
        }
    }
}
