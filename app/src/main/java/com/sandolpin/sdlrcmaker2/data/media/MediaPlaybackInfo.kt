package com.sandolpin.sdlrcmaker2.data.media

import android.graphics.Bitmap

/**
 * MediaSession経由で取得した「現在デバイス上で再生中のメディア」の情報。
 * NotificationAccessServiceがこれを生成し、MediaSessionMonitor経由でUIへ流す。
 */
data class MediaPlaybackInfo(
    val title: String = "曲名",
    val artist: String = "アーティスト名",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val packageName: String? = null,
    // 以下2つは「positionMsが観測された瞬間」からの経過時間で現在位置を推定するために使う。
    // MediaSessionは再生中も常時位置を送ってくれるわけではなく、再生/一時停止などの
    // イベント発生時にしか値を送ってこないため、これがないと再生中もUIの表示が止まって見える。
    val positionAnchorElapsedRealtime: Long = 0L,
    val playbackSpeed: Float = 1f,
)