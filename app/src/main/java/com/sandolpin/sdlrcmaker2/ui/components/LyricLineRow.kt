package com.sandolpin.sdlrcmaker2.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import com.sandolpin.sdlrcmaker2.util.TimeFormatter

/**
 * 編集画面(打刻)の歌詞行1行分。
 *
 * Shared Element Transitionについて:
 * 長押しで「行の修正」モーダルへなめらかに変形させるため、この行を囲むBoxに
 * Modifier.sharedElementWithCallerManagedVisibility(state, visible) を付与している。
 *
 * 以前はAnimatedVisibilityの enter/exit に紐づく sharedBounds を使っていたが、
 * 一覧側のAnimatedVisibilityがモーダル表示中に完全に消えてしまう構成だったため、
 * (1) 背景にモーダルの向こうの一覧が透けて見えない、(2) モーダルを閉じて戻るときに
 * 一覧側が再構築中でマッチングが間に合わず変形しない、という2つの不具合が起きていた。
 * sharedElementWithCallerManagedVisibility は「今どちらが実体として見えるべきか」を
 * isBeingEdited フラグで自前管理する方式のため、一覧側は常にコンポジションに
 * 残しておける(=背景が透ける)。編集中の行だけ visible = false にして
 * モーダル側のピルに“実体”を明け渡す形で変形させている。
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.LyricLineRow(
    line: LyricLine,
    isActive: Boolean,
    supportsEndTime: Boolean,
    isBeingEdited: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onToggleRightAlign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isComplete = if (supportsEndTime) line.hasFinished else line.hasStarted

    val containerColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 250),
        label = "lineContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 250),
        label = "lineContentColor",
    )
    val subContentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 250),
        label = "lineSubContentColor",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 250),
        label = "lineIconTint",
    )

    val targetBias = if (line.isRightAlign) 1f else -1f
    val bias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = tween(durationMillis = 250),
        label = "rightAlignBias",
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(BiasAlignment(horizontalBias = bias, verticalBias = 0f))
                .sharedElementWithCallerManagedVisibility(
                    sharedContentState = rememberSharedContentState(key = line.id),
                    visible = !isBeingEdited,
                )
                .clip(RoundedCornerShape(14.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(14.dp),
                )
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isComplete) "打刻済み" else "未打刻",
                    tint = iconTint,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                    if (line.hasStarted) {
                        val timeLabel = buildString {
                            append("開始:${TimeFormatter.format(line.startMs!!)}")
                            if (supportsEndTime && line.endMs != null) {
                                append("  終了:${TimeFormatter.format(line.endMs)}")
                            }
                        }
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = subContentColor,
                        )
                    }
                }
                IconButton(onClick = onToggleRightAlign) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = if (line.isRightAlign) "右揃え解除" else "右揃えにする",
                        tint = iconTint,
                    )
                }
            }
        }
    }
}