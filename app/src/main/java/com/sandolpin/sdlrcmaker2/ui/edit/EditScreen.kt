package com.sandolpin.sdlrcmaker2.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.ui.components.LineEditModal
import com.sandolpin.sdlrcmaker2.ui.components.LyricLineRow
import com.sandolpin.sdlrcmaker2.ui.components.MediaPlayerBar

/**
 * 打刻本体画面。
 * 上部: 歌詞行リスト(タップで開始/終了、長押しで修正モーダル)
 * 下部: メディアプレイヤー(sdlrcのみ打刻用アクションボタン付き)
 *
 * 【Shared Element Transitionの実装方針】
 * 「一覧」は常にコンポジションに残し、「モーダル」をその上に重ねて表示する構成。
 * どちらが今の“実体”として見えるべきかは、LyricLineRow/LineEditModal側で
 * Modifier.sharedElementWithCallerManagedVisibility(state, visible) により
 * isBeingEdited(=編集中かどうか)で自前管理している。
 * モーダルを包むAnimatedVisibilityは、スクリムやダイアログ枠など「shared要素ではない部分」の
 * 単純なフェードイン/フェードアウトのためだけに使っている。
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    onBack: () -> Unit,
    onProceedToOutput: () -> Unit,
) {
    val file by viewModel.lyricFile.collectAsState()
    val activeIndices by viewModel.activeIndices.collectAsState()
    val editingIndex by viewModel.editingLineIndex.collectAsState()
    val playbackInfo by viewModel.playbackInfo.collectAsState()
    val visibleButtons by viewModel.visiblePlayerButtons.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    // 戻るボタン・システムの戻る操作どちらでも、作成を中断してよいか必ず確認する。
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) { showExitConfirmDialog = true }

    // 一覧は常に表示され続けるため、スクロール位置のリセット問題も構造上自然に解消されている。
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("sdlrcを作成する", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo, enabled = canUndo) {
                        Icon(Icons.Filled.Undo, contentDescription = "元に戻す")
                    }
                    IconButton(onClick = viewModel::redo, enabled = canRedo) {
                        Icon(Icons.Filled.Redo, contentDescription = "元に戻すをキャンセル")
                    }
                    TextButton(onClick = onProceedToOutput) { Text("出力へ") }
                },
            )
        },
    ) { padding ->
        SharedTransitionLayout(modifier = Modifier.padding(padding)) {
            // --- 一覧(常に存在。モーダル表示中も裏に残ることで背景が透けて見える) ---
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "アクティブ行は色つきで表示されます。行をタップしたらその時間を" +
                            "開始時間として記録し、もう1度タップして終了時間を記録します。" +
                            ">ボタンで右揃えで表示するように記録されます。",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(file.lines, key = { it.id }) { line ->
                        val index = file.lines.indexOf(line)
                        LyricLineRow(
                            line = line,
                            isActive = index in activeIndices,
                            supportsEndTime = file.format.supportsEndTime,
                            isBeingEdited = index == editingIndex,
                            onTap = { viewModel.onTapLine(index) },
                            onLongPress = { viewModel.onLongPressLine(index) },
                            onToggleRightAlign = { viewModel.toggleRightAlign(index) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                MediaPlayerBar(
                    playbackInfo = playbackInfo,
                    onPlayPause = viewModel::onPlayPause,
                    onSkip = viewModel::onSkip,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    actionButtons = if (file.format.supportsEndTime) {
                        {
                            if (PlayerActionButton.FINISH_AND_START_NEXT in visibleButtons) {
                                Button(
                                    onClick = viewModel::finishCurrentAndStartNext,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("現在の行を終了し、次の行を開始") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (PlayerActionButton.FINISH_CURRENT in visibleButtons) {
                                    OutlinedButton(
                                        onClick = viewModel::finishCurrentLine,
                                        modifier = Modifier.weight(1f),
                                    ) { Text("現在の行を終了") }
                                }
                                if (PlayerActionButton.START_NEXT in visibleButtons) {
                                    Button(
                                        onClick = viewModel::startNextLine,
                                        modifier = Modifier.weight(1f),
                                    ) { Text("次の行を開始") }
                                }
                            }
                        }
                    } else null,
                )
            }

            // --- モーダル(スクリム+編集ダイアログ本体。ここは単純なフェードのみ担当) ---
            AnimatedVisibility(
                visible = editingIndex != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val index = editingIndex
                if (index != null) {
                    val line = file.lines.getOrNull(index)
                    if (line != null) {
                        LineEditModal(
                            line = line,
                            previousLineEndMs = file.lines.getOrNull(index - 1)?.endMs,
                            supportsEndTime = file.format.supportsEndTime,
                            supportsRightAlign = file.format.supportsRightAlign,
                            hasNextLine = index < file.lines.lastIndex,
                            onDismiss = viewModel::dismissLineEditModal,
                            onTextChange = { viewModel.updateLineText(index, it) },
                            onStartTimeChange = { viewModel.updateLineStart(index, it) },
                            onEndTimeChange = { viewModel.updateLineEnd(index, it) },
                            onAlignStartToPreviousEnd = { viewModel.alignStartToPreviousEnd(index) },
                            onToggleRightAlign = { viewModel.toggleRightAlign(index) },
                            onShowNextLine = viewModel::showNextLineInModal,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("作成を中断しますか?") },
            text = { Text("ここまでの内容は下書きとして保存されています。作成履歴から再開できます。") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmDialog = false
                    onBack()
                }) { Text("中断する") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) { Text("続ける") }
            },
        )
    }
}