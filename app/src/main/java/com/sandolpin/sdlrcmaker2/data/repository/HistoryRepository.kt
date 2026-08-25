package com.sandolpin.sdlrcmaker2.data.repository

import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import com.sandolpin.sdlrcmaker2.data.storage.HistoryDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HistoryRepository(private val dataStore: HistoryDataStore) {

    val entries: Flow<List<HistoryEntry>> = dataStore.entriesFlow

    /** 既存IDがあれば更新、なければ追加。更新日時順(新しい順)で保持する */
    suspend fun upsert(entry: HistoryEntry) {
        val current = dataStore.entriesFlow.first().toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) current[index] = entry else current.add(0, entry)
        dataStore.save(current.sortedByDescending { it.updatedAtMs })
    }

    suspend fun remove(id: String) {
        val current = dataStore.entriesFlow.first().filterNot { it.id == id }
        dataStore.save(current)
    }
}
