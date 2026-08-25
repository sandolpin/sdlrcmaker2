package com.sandolpin.sdlrcmaker2.data.storage

import android.content.Context
import android.net.Uri
import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.parser.LrcParser
import com.sandolpin.sdlrcmaker2.data.parser.LyricParser
import com.sandolpin.sdlrcmaker2.data.parser.SdlrcParser
import java.io.File

/**
 * ファイルの実I/Oを担当する。
 * - 下書き(作業中データ)はアプリ内部ストレージ(filesDir/drafts)に保存し、履歴画面から再開できるようにする
 * - 最終出力(エクスポート)はStorage Access Framework経由でユーザーが選んだ場所に書き出す
 */
class FileStorageManager(private val context: Context) {

    private val draftsDir: File
        get() = File(context.filesDir, "drafts").apply { if (!exists()) mkdirs() }

    fun parserFor(format: FileFormat): LyricParser = when (format) {
        FileFormat.SDLRC -> SdlrcParser()
        FileFormat.LRC -> LrcParser()
    }

    /** 下書きをアプリ内部に保存し、保存先パスを返す */
    fun saveDraft(file: LyricFile, existingPath: String? = null): String {
        val parser = parserFor(file.format)
        val content = parser.serialize(file)
        val target = existingPath?.let { File(it) }
            ?: File(draftsDir, "${System.currentTimeMillis()}_${file.title.ifBlank { "untitled" }}.${file.format.extension}")
        target.writeText(content)
        return target.absolutePath
    }

    /** 下書きパスからLyricFileを読み込む */
    fun loadDraft(path: String, format: FileFormat): LyricFile {
        val content = File(path).readText()
        return parserFor(format).parse(content)
    }

    fun deleteDraft(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    /** SAFで選択したUriへエクスポート(書き込み) */
    fun exportTo(uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    /** SAFで選択したUriからインポート(読み込み) */
    fun importFrom(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("ファイルを読み込めませんでした")
    }

    /**
     * ファイル形式(sdlrc/lrc)を判定する。
     *
     * ファイル名の拡張子だけで判定すると、何らかの理由で拡張子が変わってしまったファイル
     * (例: 過去のバージョンのバグで ".sdlrc.txt" のように保存されてしまったファイルなど)を
     * 誤って lrc として読み込んでしまい、sdlrc特有の「[開始][/終了]」という2つ目の
     * タイムスタンプが歌詞テキストに残ってしまう(=終了時刻が歌詞に埋まる)原因になる。
     * そのため、拡張子を優先しつつも、内容に sdlrc特有のパターンが含まれていれば
     * それを優先して sdlrc と判定するようにしている。
     */
    fun detectFormat(fileName: String, content: String): FileFormat {
        val lowerName = fileName.lowercase()
        return when {
            lowerName.contains(".${FileFormat.SDLRC.extension}") -> FileFormat.SDLRC
            SDLRC_SIGNATURE_REGEX.containsMatchIn(content) -> FileFormat.SDLRC
            else -> FileFormat.LRC
        }
    }

    companion object {
        // sdlrc特有の「[開始時刻(/r任意)][/終了時刻]」という2連タイムスタンプの並びを検出する
        private val SDLRC_SIGNATURE_REGEX =
            Regex("""\[\d{1,3}:\d{2}\.\d{2}(/r)?]\[/\d{1,3}:\d{2}\.\d{2}]""")
    }
}