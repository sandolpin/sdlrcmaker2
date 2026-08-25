package com.sandolpin.sdlrcmaker2.ui.create

import androidx.lifecycle.ViewModel
import com.sandolpin.sdlrcmaker2.data.media.MediaSessionMonitor
import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateInputViewModel(
    val format: FileFormat,
    private val mediaSessionMonitor: MediaSessionMonitor,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _artist = MutableStateFlow("")
    val artist: StateFlow<String> = _artist.asStateFlow()

    private val _lyricsText = MutableStateFlow("")
    val lyricsText: StateFlow<String> = _lyricsText.asStateFlow()

    fun onTitleChange(value: String) { _title.value = value }
    fun onArtistChange(value: String) { _artist.value = value }
    fun onLyricsChange(value: String) { _lyricsText.value = value }

    /** 「再生中の曲のデータを貼り付ける」: MediaSessionから曲名/アーティストを取得して反映する */
    fun pasteFromNowPlaying() {
        val info = mediaSessionMonitor.playbackInfo.value ?: return
        _title.value = info.title
        _artist.value = info.artist
    }

    fun canProceed(): Boolean = _title.value.isNotBlank() && _lyricsText.value.isNotBlank()

    /** 歌詞テキストを行ごとに分割してLyricFileを組み立てる(空行は除く) */
    fun buildLyricFile(): LyricFile {
        val lines = _lyricsText.value
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { LyricLine(text = it) }
        return LyricFile(
            title = _title.value.trim(),
            artist = _artist.value.trim(),
            format = format,
            lines = lines,
        )
    }
}
