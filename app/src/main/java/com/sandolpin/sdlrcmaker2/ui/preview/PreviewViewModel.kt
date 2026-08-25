package com.sandolpin.sdlrcmaker2.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.sdlrcmaker2.data.media.MediaPlaybackInfo
import com.sandolpin.sdlrcmaker2.data.media.MediaSessionMonitor
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class PreviewViewModel(
    val lyricFile: LyricFile,
    private val mediaSessionMonitor: MediaSessionMonitor,
) : ViewModel() {

    val playbackInfo: StateFlow<MediaPlaybackInfo?> = mediaSessionMonitor.playbackInfo

    /**
     * 現在の再生位置にもとづき、カラオケ風にハイライトすべき行のインデックス。
     * MediaSessionは再生中も常時位置をpushしてくれるわけではないため、playbackInfoの変化を
     * 待つのではなく、一定間隔(250ms)でmediaSessionMonitor.currentPositionMs()を
     * ライブ計算し直すティッカーを使っている。
     */
    val activeLineIndex: StateFlow<Int> = flow {
        while (true) {
            val position = mediaSessionMonitor.currentPositionMs()
            val index = lyricFile.lines.indexOfLast { line ->
                val start = line.startMs ?: return@indexOfLast false
                start <= position
            }
            emit(index.coerceAtLeast(0))
            delay(250)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun onPlayPause() {
        val playing = playbackInfo.value?.isPlaying == true
        if (playing) mediaSessionMonitor.pause() else mediaSessionMonitor.play()
    }

    fun onSkip(deltaMs: Long) = mediaSessionMonitor.skip(deltaMs)
}