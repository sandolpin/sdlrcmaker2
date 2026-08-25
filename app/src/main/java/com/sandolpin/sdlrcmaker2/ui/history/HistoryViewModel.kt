package com.sandolpin.sdlrcmaker2.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import com.sandolpin.sdlrcmaker2.data.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    val entries: StateFlow<List<HistoryEntry>> = historyRepository.entries
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun delete(entry: HistoryEntry) {
        viewModelScope.launch { historyRepository.remove(entry.id) }
    }
}
