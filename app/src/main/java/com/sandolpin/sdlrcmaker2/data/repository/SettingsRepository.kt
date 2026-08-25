package com.sandolpin.sdlrcmaker2.data.repository

import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.data.storage.SettingsDataStore
import com.sandolpin.sdlrcmaker2.ui.theme.DarkModeOption
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    val darkModeOption: Flow<DarkModeOption> = dataStore.darkModeOptionFlow
    val visiblePlayerButtons: Flow<Set<PlayerActionButton>> = dataStore.visiblePlayerButtonsFlow

    suspend fun setDarkModeOption(option: DarkModeOption) = dataStore.setDarkModeOption(option)

    suspend fun setVisiblePlayerButtons(buttons: Set<PlayerActionButton>) =
        dataStore.setVisiblePlayerButtons(buttons)
}
