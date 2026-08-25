package com.sandolpin.sdlrcmaker2.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sandolpin.sdlrcmaker2.data.model.PlayerActionButton
import com.sandolpin.sdlrcmaker2.ui.theme.DarkModeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** 設定値(ダークモード・編集画面のボタン表示)の永続化 */
class SettingsDataStore(private val context: Context) {

    private val darkModeKey = stringPreferencesKey("dark_mode_option")
    private val visibleButtonsKey = stringSetPreferencesKey("visible_player_buttons")

    val darkModeOptionFlow: Flow<DarkModeOption> = context.settingsDataStore.data.map { prefs ->
        prefs[darkModeKey]?.let { saved ->
            runCatching { DarkModeOption.valueOf(saved) }.getOrNull()
        } ?: DarkModeOption.SYSTEM
    }

    /** 未設定時はすべてのボタンを表示するデフォルトにする */
    val visiblePlayerButtonsFlow: Flow<Set<PlayerActionButton>> =
        context.settingsDataStore.data.map { prefs ->
            prefs[visibleButtonsKey]?.mapNotNull { name ->
                runCatching { PlayerActionButton.valueOf(name) }.getOrNull()
            }?.toSet() ?: PlayerActionButton.entries.toSet()
        }

    suspend fun setDarkModeOption(option: DarkModeOption) {
        context.settingsDataStore.edit { it[darkModeKey] = option.name }
    }

    suspend fun setVisiblePlayerButtons(buttons: Set<PlayerActionButton>) {
        context.settingsDataStore.edit { it[visibleButtonsKey] = buttons.map { b -> b.name }.toSet() }
    }
}
