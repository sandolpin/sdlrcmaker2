package com.sandolpin.sdlrcmaker2.ui.removestamp

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.repository.LyricFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemoveTimestampViewModel(
    private val lyricFileRepository: LyricFileRepository,
) : ViewModel() {

    private val _loadedFile = MutableStateFlow<LyricFile?>(null)
    val loadedFile: StateFlow<LyricFile?> = _loadedFile.asStateFlow()

    val plainText: StateFlow<String> = _loadedFile
        .map { file -> file?.let { lyricFileRepository.toPlainText(it) } ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun loadFromUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            runCatching { lyricFileRepository.importLyricFile(uri, fileName) }
                .onSuccess { _loadedFile.value = it }
        }
    }

    suspend fun saveAsTextFile(uri: Uri) {
        val file = _loadedFile.value ?: return
        lyricFileRepository.exportPlainText(uri, file)
    }
}
