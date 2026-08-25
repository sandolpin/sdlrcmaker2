package com.sandolpin.sdlrcmaker2.data.model

import java.util.UUID

/**
 * 歌詞1行分のデータ。
 *
 * @param id            行を一意に識別するID(リスト並べ替え・編集の追跡用)
 * @param text          歌詞テキスト
 * @param startMs       開始時刻(ミリ秒)。未打刻の場合はnull
 * @param endMs         終了時刻(ミリ秒)。lrc形式では常にnull。sdlrcでも未打刻ならnull
 * @param isRightAlign  右揃え(/r)表示するかどうか。sdlrcのみ有効
 */
data class LyricLine(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startMs: Long? = null,
    val endMs: Long? = null,
    val isRightAlign: Boolean = false,
) {
    /** 開始のみ打刻済み(sdlrcで終了待ちの状態、またはlrcで打刻完了の状態) */
    val hasStarted: Boolean get() = startMs != null

    /** 開始・終了ともに打刻済み(sdlrcの完了状態) */
    val hasFinished: Boolean get() = startMs != null && endMs != null
}
