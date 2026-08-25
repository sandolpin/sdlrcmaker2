package com.sandolpin.sdlrcmaker2.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 「作成履歴」画面に表示する1件分のデータ。
 *
 * @param id            履歴を一意に識別するID
 * @param title         タイトル(表示名)
 * @param format        sdlrc / lrc
 * @param updatedAtMs   最終更新日時(epoch millis)
 * @param filePath      アプリ内保存領域における実ファイルパス(作業再開用の下書き)
 */
@Serializable
data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val format: FileFormat,
    val updatedAtMs: Long,
    val filePath: String,
)
