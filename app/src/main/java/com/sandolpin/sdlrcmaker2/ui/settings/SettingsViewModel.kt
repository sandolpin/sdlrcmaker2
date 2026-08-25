package com.sandolpin.sdlrcmaker2.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.data.repository.SettingsRepository
import com.sandolpin.sdlrcmaker2.ui.theme.DarkModeOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val darkModeOption: StateFlow<DarkModeOption> = settingsRepository.darkModeOption
        .stateIn(viewModelScope, SharingStarted.Eagerly, DarkModeOption.SYSTEM)

    val visiblePlayerButtons: StateFlow<Set<PlayerActionButton>> = settingsRepository.visiblePlayerButtons
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerActionButton.entries.toSet())

    fun setDarkModeOption(option: DarkModeOption) {
        viewModelScope.launch { settingsRepository.setDarkModeOption(option) }
    }

    fun toggleButtonVisibility(button: PlayerActionButton) {
        viewModelScope.launch {
            val current = visiblePlayerButtons.value
            val updated = if (button in current) current - button else current + button
            settingsRepository.setVisiblePlayerButtons(updated)
        }
    }
}
