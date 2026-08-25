package com.sandolpin.sdlrcmaker2.data.media

import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModelからMediaSession連携を扱うための窓口。
 * 実体はNotificationAccessService(常駐サービス)が保持しているため、
 * ここではその公開StateFlowと操作関数への薄いブリッジを提供するのみ。
 *
 * 通知アクセス権限が許可されていない場合、playbackInfoは常にnullのままとなる。
 * 権限チェックは util.PermissionUtils を参照。
 */
class MediaSessionMonitor {

    val playbackInfo: StateFlow<MediaPlaybackInfo?> = NotificationAccessService.playbackInfo

    /** 「今この瞬間」の再生位置(打刻・シーク計算は必ずこちらを使う) */
    fun currentPositionMs(): Long =
        NotificationAccessService.instanceRef?.currentPositionMs() ?: playbackInfo.value?.positionMs ?: 0L

    fun play() = NotificationAccessService.instanceRef?.play()
    fun pause() = NotificationAccessService.instanceRef?.pause()
    fun seekTo(positionMs: Long) = NotificationAccessService.instanceRef?.seekTo(positionMs)

    fun skip(deltaMs: Long) {
        val current = currentPositionMs()
        seekTo((current + deltaMs).coerceAtLeast(0L))
    }
}