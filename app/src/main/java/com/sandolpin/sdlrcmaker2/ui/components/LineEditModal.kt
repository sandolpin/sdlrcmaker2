package com.sandolpin.sdlrcmaker2.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import com.sandolpin.sdlrcmaker2.util.TimeFormatter

/**
 * 歌詞行を長押ししたときに開く修正モーダル。
 *
 * Shared Element Transitionは「歌詞テキストの入った青いピル部分」だけに適用している
 * (LyricLineRowの青いアクティブ表示と同じkey=line.id・同じ見た目)。
 * ピル部分は Modifier.sharedElementWithCallerManagedVisibility(state, visible = true) を
 * 使っており、このモーダルが存在している間は常に「実体」として扱われる
 * (一覧側は同じkeyの行を isBeingEdited=true として visible=false にしているため、
 * 見た目上そちらからこちらへ変形しているように見える)。
 * 白いダイアログの背景・戻るボタン・時間欄・下部ボタンなどはshared要素ではなく、
 * EditScreen側でこのComposable全体を包んでいるAnimatedVisibilityの通常のフェードで
 * 表示/非表示される。
 *
 * lrc形式の場合、終了時間ボックス・「開始時間を前の行の終了時間に合わせる」・
 * 「右に表示する」は概念自体が存在しないため非表示にする。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineEditModal(
    line: LyricLine,
    previousLineEndMs: Long?,
    supportsEndTime: Boolean,
    supportsRightAlign: Boolean,
    hasNextLine: Boolean,
    onDismiss: () -> Unit,
    onTextChange: (String) -> Unit,
    onStartTimeChange: (Long) -> Unit,
    onEndTimeChange: (Long) -> Unit,
    onAlignStartToPreviousEnd: () -> Unit,
    onToggleRightAlign: () -> Unit,
    onShowNextLine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickableNoRipple(onDismiss),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickableNoRipple { /* 内部タップは伝播させない */ }
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "閉じる")
                }
                Text("行の修正", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ここが一覧の青いカードとShared Element Transitionでつながる「本体」部分
            var text by remember(line.id) { mutableStateOf(line.text) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElementWithCallerManagedVisibility(
                        sharedContentState = rememberSharedContentState(key = line.id),
                        visible = true,
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onTextChange(it)
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "ボックス内をタップして編集できます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TimeEditField(
                    label = "開始時間",
                    ms = line.startMs,
                    onCommit = onStartTimeChange,
                    modifier = Modifier.weight(1f),
                )
                if (supportsEndTime) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TimeEditField(
                        label = "終了時間",
                        ms = line.endMs,
                        onCommit = onEndTimeChange,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (supportsEndTime) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAlignStartToPreviousEnd, modifier = Modifier.fillMaxWidth()) {
                    Text("開始時間を前の行の終了時間に合わせる")
                }
                if (previousLineEndMs != null) {
                    Text(
                        text = "前の行は${TimeFormatter.format(previousLineEndMs)}で終了しています",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (supportsRightAlign) {
                    OutlinedButton(onClick = onToggleRightAlign, modifier = Modifier.weight(1f)) {
                        Text(if (line.isRightAlign) "右揃えを解除" else "右に表示する")
                    }
                }
                if (hasNextLine) {
                    Button(onClick = onShowNextLine, modifier = Modifier.weight(1f)) {
                        Text("次の行を表示")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeEditField(
    label: String,
    ms: Long?,
    onCommit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember(ms) { mutableStateOf(ms?.let { TimeFormatter.format(it) } ?: "00:00.00") }
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            value = input
            TimeFormatter.parse(input)?.let(onCommit)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
    )
}

/** リップルなしのクリック領域(スクリムの背後タップでの誤爆演出を避けるため) */
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(indication = null, interactionSource = interactionSource, onClick = onClick)
}