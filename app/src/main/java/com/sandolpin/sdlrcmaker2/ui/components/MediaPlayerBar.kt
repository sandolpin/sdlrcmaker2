package com.sandolpin.sdlrcmaker2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.media.MediaPlaybackInfo
import com.sandolpin.sdlrcmaker2.util.TimeFormatter

/**
 * 端末で現在再生中のメディア(MediaSession経由)を表示・操作するプレイヤーバー。
 * 編集画面では actionButtons スロットに打刻用ボタン群を差し込む。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerBar(
    playbackInfo: MediaPlaybackInfo?,
    onPlayPause: () -> Unit,
    onSkip: (deltaMs: Long) -> Unit,
    modifier: Modifier = Modifier,
    actionButtons: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val info = playbackInfo ?: MediaPlaybackInfo()

    // MediaSessionは再生中でも常に位置をpushしてくれるわけではないため、
    // isPlayingの間だけこちらで定期的に「経過時間分を加算した現在位置」を再計算して表示する。
    var nowElapsed by remember(info.isPlaying) { mutableStateOf(android.os.SystemClock.elapsedRealtime()) }
    LaunchedEffect(info.isPlaying, info.positionAnchorElapsedRealtime) {
        while (info.isPlaying) {
            nowElapsed = android.os.SystemClock.elapsedRealtime()
            kotlinx.coroutines.delay(200)
        }
    }
    val displayPositionMs = if (info.isPlaying) {
        val elapsed = nowElapsed - info.positionAnchorElapsedRealtime
        (info.positionMs + (elapsed * info.playbackSpeed).toLong()).coerceAtLeast(0L)
    } else {
        info.positionMs
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val art = info.albumArt
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (art != null) {
                    androidx.compose.foundation.Image(
                        bitmap = art.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = info.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val durationSafe = info.durationMs.coerceAtLeast(1L)
        // デフォルトのSliderはトラックが太いため、track スロットを差し替えて
        // 高さ4dpの細いトラックに変更している(つまみの大きさ自体はデフォルトのまま)。
        Slider(
            value = (displayPositionMs.toFloat() / durationSafe.toFloat()).coerceIn(0f, 1f),
            onValueChange = { fraction -> onSkip(((fraction * durationSafe).toLong()) - displayPositionMs) },
            modifier = Modifier.fillMaxWidth(),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                )
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(TimeFormatter.formatShort(displayPositionMs), style = MaterialTheme.typography.bodySmall)
            Text(TimeFormatter.formatShort(info.durationMs), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onSkip(-10_000L) }) {
                Icon(Icons.Filled.Replay10, contentDescription = "10秒戻る")
            }
            IconButton(onClick = { onSkip(-5_000L) }) {
                Icon(Icons.Filled.Replay5, contentDescription = "5秒戻る")
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (info.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (info.isPlaying) "一時停止" else "再生",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            IconButton(onClick = { onSkip(5_000L) }) {
                Icon(Icons.Filled.Forward5, contentDescription = "5秒送り")
            }
            IconButton(onClick = { onSkip(10_000L) }) {
                Icon(Icons.Filled.Forward10, contentDescription = "10秒送り")
            }
        }

        if (actionButtons != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = actionButtons,
            )
        }
    }
}