package com.sandolpin.sdlrcmaker2.data.media

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 通知アクセス権限(Notification Listener)を利用して、他アプリのMediaSessionを取得するサービス。
 *
 * Androidの制約上、他アプリの再生状態(曲名・再生位置・再生/一時停止の操作)を取得するには
 * MediaSessionManager だけでなく、この NotificationListenerService の実装とユーザーによる
 * 「通知へのアクセス」許可(設定アプリでの手動許可)が必須になる。
 * 権限が許可されていない間、このサービスの各コールバックは呼ばれない。
 */
class NotificationAccessService : NotificationListenerService() {

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushInfo(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushInfo(activeController)
        }

        override fun onSessionDestroyed() {
            _playbackInfo.value = null
        }
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            attachToController(controllers?.firstOrNull())
        }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val manager = getSystemService(MediaSessionManager::class.java)
        val componentName = ComponentName(this, NotificationAccessService::class.java)
        manager.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
        attachToController(manager.getActiveSessions(componentName).firstOrNull())
    }

    private fun attachToController(controller: MediaController?) {
        if (activeController?.sessionToken == controller?.sessionToken) return
        activeController?.unregisterCallback(controllerCallback)
        activeController = controller
        controller?.registerCallback(controllerCallback)
        pushInfo(controller)
    }

    private fun pushInfo(controller: MediaController?) {
        if (controller == null) {
            _playbackInfo.value = null
            return
        }
        val metadata = controller.metadata
        val state = controller.playbackState
        _playbackInfo.value = MediaPlaybackInfo(
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "曲名",
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "アーティスト名",
            albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART),
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
            positionMs = state?.position ?: 0L,
            durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            packageName = controller.packageName,
            // PlaybackStateのlastPositionUpdateTimeはSystemClock.elapsedRealtime()基準の値
            positionAnchorElapsedRealtime = state?.lastPositionUpdateTime
                ?: android.os.SystemClock.elapsedRealtime(),
            playbackSpeed = state?.playbackSpeed ?: 1f,
        )
    }

    /**
     * 「今この瞬間」の再生位置をライブで計算する。
     * PlaybackStateは再生中でも常に最新値をpushしてくれるわけではないため、
     * 最後に受け取ったposition(anchor)からの経過時間を再生速度分だけ加算して推定する。
     * 打刻(タイムスタンプ記録)は必ずこの関数の値を使うことで、ズレのない時刻を記録できる。
     */
    fun currentPositionMs(): Long {
        val controller = activeController ?: return _playbackInfo.value?.positionMs ?: 0L
        val state = controller.playbackState ?: return 0L
        if (state.state != PlaybackState.STATE_PLAYING) return state.position
        val elapsed = android.os.SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
        return state.position + (elapsed * state.playbackSpeed).toLong()
    }

    override fun onDestroy() {
        activeController?.unregisterCallback(controllerCallback)
        _playbackInfo.value = null
        super.onDestroy()
    }

    companion object {
        private val _playbackInfo = MutableStateFlow<MediaPlaybackInfo?>(null)

        /** 現在の再生情報。サービスが未接続/権限未許可の間はnull */
        val playbackInfo: StateFlow<MediaPlaybackInfo?> = _playbackInfo.asStateFlow()

        // 各操作は現在アタッチされているMediaControllerに直接委譲するため
        // サービスのインスタンス参照が必要になる。サービスは常駐のためシングルトン的に保持する。
        internal var instanceRef: NotificationAccessService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instanceRef = this
    }

    fun play() = activeController?.transportControls?.play()
    fun pause() = activeController?.transportControls?.pause()
    fun seekTo(positionMs: Long) = activeController?.transportControls?.seekTo(positionMs)
}