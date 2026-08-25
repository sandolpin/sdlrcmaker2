package com.sandolpin.sdlrcmaker2.data.repository

import android.net.Uri
import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.storage.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 歌詞ファイルに関する処理の窓口。
 * ViewModelはこのRepositoryのみを介してファイルI/Oを行い、実装詳細(FileStorageManager)を意識しない。
 */
class LyricFileRepository(private val storageManager: FileStorageManager) {

    suspend fun saveDraft(file: LyricFile, existingPath: String? = null): String =
        withContext(Dispatchers.IO) { storageManager.saveDraft(file, existingPath) }

    suspend fun loadDraft(path: String, format: FileFormat): LyricFile =
        withContext(Dispatchers.IO) { storageManager.loadDraft(path, format) }

    suspend fun deleteDraft(path: String) =
        withContext(Dispatchers.IO) { storageManager.deleteDraft(path) }

    /** タイムスタンプを除いたプレーンテキスト(歌詞本文のみ)を生成する */
    fun toPlainText(file: LyricFile): String =
        file.lines.joinToString(separator = "\n") { it.text }

    suspend fun exportLyricFile(uri: Uri, file: LyricFile) = withContext(Dispatchers.IO) {
        val content = storageManager.parserFor(file.format).serialize(file)
        storageManager.exportTo(uri, content)
    }

    suspend fun exportPlainText(uri: Uri, file: LyricFile) = withContext(Dispatchers.IO) {
        storageManager.exportTo(uri, toPlainText(file))
    }

    suspend fun importLyricFile(uri: Uri, fileName: String): LyricFile = withContext(Dispatchers.IO) {
        val content = storageManager.importFrom(uri)
        val format = storageManager.detectFormat(fileName, content)
        storageManager.parserFor(format).parse(content)
    }
}