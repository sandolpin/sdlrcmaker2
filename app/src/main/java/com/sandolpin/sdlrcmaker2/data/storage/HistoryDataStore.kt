package com.sandolpin.sdlrcmaker2.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.historyDataStore by preferencesDataStore(name = "history")

/**
 * 作成履歴一覧をJSON文字列としてPreferences DataStoreに保存する。
 * 件数が少ないため単純なJSONリストで十分と判断している。
 */
class HistoryDataStore(private val context: Context) {

    private val key = stringPreferencesKey("history_entries_json")
    private val json = Json { ignoreUnknownKeys = true }

    val entriesFlow: Flow<List<HistoryEntry>> = context.historyDataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<HistoryEntry>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun save(entries: List<HistoryEntry>) {
        context.historyDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(entries)
        }
    }
}
