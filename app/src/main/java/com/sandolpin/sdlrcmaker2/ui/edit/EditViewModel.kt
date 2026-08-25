package com.sandolpin.sdlrcmaker2.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.sdlrcmaker2.data.media.MediaPlaybackInfo
import com.sandolpin.sdlrcmaker2.data.media.MediaSessionMonitor
import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.data.repository.HistoryRepository
import com.sandolpin.sdlrcmaker2.data.repository.LyricFileRepository
import com.sandolpin.sdlrcmaker2.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 打刻画面(EditScreen)の状態と操作をまとめたViewModel。
 *
 * ここが最も複雑な部分。ポイントは以下の3つ:
 *   1. 「今アクティブ(記録中)な行」は単一のindexではなく `activeIndices: Set<Int>` で管理している。
 *      sdlrcでは、開始だけ記録して終了をまだ記録していない行が同時に複数存在しうる
 *      (例: メインの歌詞と、それに重なる合いの手「Woo〜」を別々に記録したい場合など)。
 *      そのため「この行は今まさに開始〜終了の記録待ちである」という状態を行ごとの集合として持ち、
 *      複数の行が同時に青くハイライトされることを許容している。
 *   2. sdlrc(開始・終了あり)とlrc(開始のみ)で打刻の振る舞いが変わるため、
 *      format.supportsEndTime で分岐する。lrcは開始のみで記録が完結するため、
 *      そもそも activeIndices には加えず(ハイライトを残さず)即座に完了させている。
 *   3. Undo/Redo: すべての行編集操作は最終的に updateLines() を経由するため、
 *      そこ1箇所で「変更前の状態(歌詞行+activeIndices)」を undoStack に積んでいる。
 *      activeIndicesも一緒に保存しているのは、行データだけ戻してハイライトがズレたまま
 *      になる(=見た目上どの行が記録中か分からなくなる)不具合を避けるため。
 *      新規操作が発生した時点で redoStack は破棄する(一般的なUndo/Redoの挙動)。
 */
class EditViewModel(
    initialFile: LyricFile,
    private var draftPath: String?,
    private val historyEntryId: String,
    private val lyricFileRepository: LyricFileRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaSessionMonitor: MediaSessionMonitor,
) : ViewModel() {

    /** Undo/Redoの1スナップショット分(歌詞行+アクティブ行集合をセットで保持) */
    private data class EditHistoryState(
        val lines: List<LyricLine>,
        val activeIndices: Set<Int>,
    )

    private val _lyricFile = MutableStateFlow(initialFile)
    val lyricFile: StateFlow<LyricFile> = _lyricFile.asStateFlow()

    /** 現在「記録中(開始済み・終了待ち)」として青くハイライトされている行のindex集合。sdlrcは複数同時に持てる */
    private val _activeIndices = MutableStateFlow<Set<Int>>(emptySet())
    val activeIndices: StateFlow<Set<Int>> = _activeIndices.asStateFlow()

    /** 下部の「現在の行を終了」等のボタンが対象とする、直近操作した行のindex(ボタン専用のカーソル) */
    private val _cursorIndex = MutableStateFlow(0)

    private val _editingLineIndex = MutableStateFlow<Int?>(null)
    val editingLineIndex: StateFlow<Int?> = _editingLineIndex.asStateFlow()

    val playbackInfo: StateFlow<MediaPlaybackInfo?> = mediaSessionMonitor.playbackInfo

    val visiblePlayerButtons: StateFlow<Set<PlayerActionButton>> =
        settingsRepository.visiblePlayerButtons.stateIn(
            viewModelScope, SharingStarted.Eagerly, PlayerActionButton.entries.toSet(),
        )

    // --- Undo/Redo ---
    private val undoStack = mutableListOf<EditHistoryState>()
    private val redoStack = mutableListOf<EditHistoryState>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun currentHistoryState() =
        EditHistoryState(_lyricFile.value.lines, _activeIndices.value)

    private fun applyHistoryState(state: EditHistoryState) {
        _lyricFile.value = _lyricFile.value.copy(lines = state.lines)
        _activeIndices.value = state.activeIndices
        persistDraft()
    }

    private fun updateHistoryFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    /** 変更前の状態をundoStackへ積み、新規操作なのでredoStackは破棄する */
    private fun pushHistory() {
        undoStack.add(currentHistoryState())
        redoStack.clear()
        updateHistoryFlags()
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.add(currentHistoryState())
        applyHistoryState(previous)
        updateHistoryFlags()
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.add(currentHistoryState())
        applyHistoryState(next)
        updateHistoryFlags()
    }

    private fun currentPositionMs(): Long = mediaSessionMonitor.currentPositionMs()

    private fun updateLines(transform: (List<LyricLine>) -> List<LyricLine>) {
        pushHistory()
        _lyricFile.value = _lyricFile.value.copy(lines = transform(_lyricFile.value.lines))
        persistDraft()
    }

    /**
     * 行タップ。
     * - まだ記録中でない行(activeIndicesに含まれない行)をタップした場合:
     *   その場で開始時刻を記録し、同時に青くハイライトする(1回目のタップで選択+開始時刻の記録を同時に行う)。
     *   すでに打刻完了している行を選んだ場合は、開始時刻からの再記録として扱う(再編集)。
     *   sdlrcでは他の行が記録中であってもそのまま追加されるため、複数の行を同時にハイライトできる。
     *   lrc(終了時刻の概念がない)の場合はこの1回のタップで記録が完了するため、ハイライトは残さない。
     * - すでに記録中(このタップより前に開始だけ記録済み)の行を再タップした場合:
     *   終了時刻を記録して完了とし、ハイライトを解除する。
     */
    fun onTapLine(index: Int) {
        val file = _lyricFile.value
        val line = file.lines.getOrNull(index) ?: return
        val position = currentPositionMs()
        val isRecording = index in _activeIndices.value

        _cursorIndex.value = index

        if (!isRecording) {
            // 新規に記録開始(すでに完了済みなら開始時刻からの再記録として扱う)
            setLine(index, line.copy(startMs = position, endMs = null))
            if (file.format.supportsEndTime) {
                _activeIndices.value += index
            }
            return
        }

        // 記録中の行を再タップ → 終了時刻を記録して完了
        setLine(index, line.copy(endMs = position))
        _activeIndices.value -= index
    }

    fun onLongPressLine(index: Int) {
        _editingLineIndex.value = index
    }

    fun dismissLineEditModal() {
        _editingLineIndex.value = null
    }

    fun showNextLineInModal() {
        val current = _editingLineIndex.value ?: return
        if (current < _lyricFile.value.lines.lastIndex) _editingLineIndex.value = current + 1
    }

    fun updateLineText(index: Int, text: String) {
        val line = _lyricFile.value.lines.getOrNull(index) ?: return
        setLine(index, line.copy(text = text))
    }

    fun updateLineStart(index: Int, ms: Long) {
        val line = _lyricFile.value.lines.getOrNull(index) ?: return
        setLine(index, line.copy(startMs = ms))
    }

    fun updateLineEnd(index: Int, ms: Long) {
        val line = _lyricFile.value.lines.getOrNull(index) ?: return
        setLine(index, line.copy(endMs = ms))
    }

    fun alignStartToPreviousEnd(index: Int) {
        val lines = _lyricFile.value.lines
        val previousEnd = lines.getOrNull(index - 1)?.endMs ?: return
        val line = lines.getOrNull(index) ?: return
        setLine(index, line.copy(startMs = previousEnd))
    }

    fun toggleRightAlign(index: Int) {
        val line = _lyricFile.value.lines.getOrNull(index) ?: return
        setLine(index, line.copy(isRightAlign = !line.isRightAlign))
    }

    private fun setLine(index: Int, newLine: LyricLine) {
        updateLines { lines -> lines.toMutableList().also { it[index] = newLine } }
    }

    // --- sdlrc専用: プレイヤー下部のアクションボタン(_cursorIndexを起点に順番に進める) ---

    /** 現在の行を終了し、続けて次の行の開始も記録する(効率化ボタン) */
    fun finishCurrentAndStartNext() {
        val position = currentPositionMs()
        val index = _cursorIndex.value
        val lines = _lyricFile.value.lines
        val current = lines.getOrNull(index) ?: return
        val nextIndex = index + 1
        val next = lines.getOrNull(nextIndex)

        updateLines { list ->
            list.toMutableList().apply {
                this[index] = current.copy(endMs = position)
                if (next != null) this[nextIndex] = next.copy(startMs = position, endMs = null)
            }
        }
        _activeIndices.value -= index
        if (next != null) {
            _activeIndices.value += nextIndex
            _cursorIndex.value = nextIndex
        }
    }

    fun finishCurrentLine() {
        val position = currentPositionMs()
        val index = _cursorIndex.value
        val current = _lyricFile.value.lines.getOrNull(index) ?: return
        setLine(index, current.copy(endMs = position))
        _activeIndices.value -= index
    }

    fun startNextLine() {
        val position = currentPositionMs()
        val nextIndex = _cursorIndex.value + 1
        val next = _lyricFile.value.lines.getOrNull(nextIndex) ?: return
        setLine(nextIndex, next.copy(startMs = position, endMs = null))
        _activeIndices.value += nextIndex
        _cursorIndex.value = nextIndex
    }

    // --- プレイヤー操作 ---
    fun onPlayPause() {
        val playing = playbackInfo.value?.isPlaying == true
        if (playing) mediaSessionMonitor.pause() else mediaSessionMonitor.play()
    }

    fun onSkip(deltaMs: Long) = mediaSessionMonitor.skip(deltaMs)

    // --- 保存 ---
    private fun persistDraft() {
        viewModelScope.launch {
            val file = _lyricFile.value
            val path = lyricFileRepository.saveDraft(file, draftPath)
            draftPath = path
            historyRepository.upsert(
                HistoryEntry(
                    id = historyEntryId,
                    title = file.title.ifBlank { "無題" },
                    format = file.format,
                    updatedAtMs = System.currentTimeMillis(),
                    filePath = path,
                ),
            )
        }
    }

    fun currentDraftPath(): String? = draftPath
}