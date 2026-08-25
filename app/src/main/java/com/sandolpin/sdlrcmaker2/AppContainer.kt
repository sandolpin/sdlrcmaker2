package com.sandolpin.sdlrcmaker2

import android.content.Context
import com.sandolpin.sdlrcmaker2.data.media.MediaSessionMonitor
import com.sandolpin.sdlrcmaker2.data.repository.HistoryRepository
import com.sandolpin.sdlrcmaker2.data.repository.LyricFileRepository
import com.sandolpin.sdlrcmaker2.data.repository.SettingsRepository
import com.sandolpin.sdlrcmaker2.data.storage.FileStorageManager
import com.sandolpin.sdlrcmaker2.data.storage.HistoryDataStore
import com.sandolpin.sdlrcmaker2.data.storage.SettingsDataStore

/**
 * 依存関係を組み立てて共有する簡易コンテナ。
 * Hiltは使わず、Applicationクラスからシングルトンとして各ViewModelに渡す構成にしている。
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val lyricFileRepository: LyricFileRepository by lazy {
        LyricFileRepository(FileStorageManager(appContext))
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(HistoryDataStore(appContext))
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(SettingsDataStore(appContext))
    }

    val mediaSessionMonitor: MediaSessionMonitor by lazy { MediaSessionMonitor() }
}
