package com.sandolpin.sdlrcmaker2.util

/**
 * "mm:ss.xx" 形式(分:秒.1/100秒)の文字列とミリ秒を相互変換する。
 * sdlrc / lrc 両形式ともこの表記(例: 00:12.34)を採用しているため共通化している。
 */
object TimeFormatter {

    private val TIME_REGEX = Regex("""^(\d{1,3}):(\d{2})\.(\d{2})$""")

    /** ミリ秒 → "mm:ss.xx" */
    fun format(ms: Long): String {
        val totalCentiseconds = ms / 10
        val minutes = totalCentiseconds / 6000
        val seconds = (totalCentiseconds / 100) % 60
        val centiseconds = totalCentiseconds % 100
        return "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
    }

    /** "mm:ss.xx" → ミリ秒。不正な形式はnullを返す */
    fun parse(text: String): Long? {
        val match = TIME_REGEX.matchEntire(text.trim()) ?: return null
        val (minutes, seconds, centiseconds) = match.destructured
        return minutes.toLong() * 60_000L + seconds.toLong() * 1_000L + centiseconds.toLong() * 10L
    }

    /** 再生位置表示用の "m:ss" 形式(プレイヤーUIの現在時間/曲の長さ表示に使用) */
    fun formatShort(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
