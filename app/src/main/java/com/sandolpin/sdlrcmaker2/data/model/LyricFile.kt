package com.sandolpin.sdlrcmaker2.data.model

/**
 * 歌詞ファイル全体のデータ。
 * 編集画面・プレビュー画面・エクスポート処理はすべてこのオブジェクトを介して操作する。
 *
 * @param title   タイトル(ファイル名にも使用)
 * @param artist  アーティスト名。lrc形式では画面上非表示だが、値自体は保持しておいて良い
 * @param format  sdlrc / lrc
 * @param lines   歌詞行のリスト(表示順)
 */
data class LyricFile(
    val title: String,
    val artist: String = "",
    val format: FileFormat,
    val lines: List<LyricLine> = emptyList(),
) {
    /** 打刻がすべて完了しているか(sdlrcは開始+終了、lrcは開始のみが条件) */
    fun isFullyStamped(): Boolean = lines.isNotEmpty() && lines.all { line ->
        if (format.supportsEndTime) line.hasFinished else line.hasStarted
    }
}
