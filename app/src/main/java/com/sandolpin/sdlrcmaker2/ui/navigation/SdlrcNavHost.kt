package com.sandolpin.sdlrcmaker2.ui.navigation

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sandolpin.sdlrcmaker2.AppContainer
import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.HistoryEntry
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.ui.components.AppBottomBar
import com.sandolpin.sdlrcmaker2.ui.components.BottomTab
import com.sandolpin.sdlrcmaker2.ui.create.CreateInputScreen
import com.sandolpin.sdlrcmaker2.ui.create.CreateInputViewModel
import com.sandolpin.sdlrcmaker2.ui.edit.EditScreen
import com.sandolpin.sdlrcmaker2.ui.edit.EditViewModel
import com.sandolpin.sdlrcmaker2.ui.history.HistoryScreen
import com.sandolpin.sdlrcmaker2.ui.history.HistoryViewModel
import com.sandolpin.sdlrcmaker2.ui.home.HomeScreen
import com.sandolpin.sdlrcmaker2.ui.preview.PreviewScreen
import com.sandolpin.sdlrcmaker2.ui.preview.PreviewViewModel
import com.sandolpin.sdlrcmaker2.ui.removestamp.RemoveTimestampScreen
import com.sandolpin.sdlrcmaker2.ui.removestamp.RemoveTimestampViewModel
import com.sandolpin.sdlrcmaker2.ui.settings.SettingsScreen
import com.sandolpin.sdlrcmaker2.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * アプリ全体のナビゲーションを組み立てるNavHost。
 *
 * 【設計の補足】
 * 「作成フロー(タイトル入力 → 編集 → プレビュー)」は同じ LyricFile を扱い続ける必要がある。
 * Hiltなどの DI フレームワークを使わない構成のため、ここでは NavHost の外側で
 * remember した `draftFile` / `draftPath` / `historyId` を"作成中データの一時置き場"として使い、
 * 各画面に入るたびに viewModelFactory 経由でその時点の値を渡してViewModelを組み立てている。
 * 画面を進める(次へ)たびに最新の状態をこの変数へ書き戻すことで、
 * 3画面をまたいでも同じ歌詞データを編集し続けられる。
 */
@Composable
fun SdlrcNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var draftFile by remember { mutableStateOf<LyricFile?>(null) }
    var draftPath by remember { mutableStateOf<String?>(null) }
    var historyId by remember { mutableStateOf(UUID.randomUUID().toString()) }

    fun startNewCreation(format: FileFormat) {
        draftFile = null
        draftPath = null
        historyId = UUID.randomUUID().toString()
        navController.navigate(Screen.createInputRoute(format.name))
    }

    // インポート(ホーム → そのまま編集画面へ)
    val importForEditLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val fileName = queryFileName(context, uri) ?: "imported"
                val file = container.lyricFileRepository.importLyricFile(uri, fileName)
                draftFile = file
                draftPath = null
                historyId = UUID.randomUUID().toString()
                navController.navigate(Screen.EDIT)
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = when (currentRoute) {
        Screen.HISTORY -> BottomTab.HISTORY
        Screen.SETTINGS -> BottomTab.SETTINGS
        else -> BottomTab.HOME
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    val route = when (tab) {
                        BottomTab.HOME -> Screen.HOME
                        BottomTab.HISTORY -> Screen.HISTORY
                        BottomTab.SETTINGS -> Screen.SETTINGS
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.HOME,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Screen.HOME) {
                    HomeScreen(
                        onCreate = { format -> startNewCreation(format) },
                        onImport = { importForEditLauncher.launch(arrayOf("*/*")) },
                        onRemoveTimestamp = { navController.navigate(Screen.REMOVE_TIMESTAMP) },
                    )
                }

                composable(Screen.HISTORY) {
                    val vm: HistoryViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { HistoryViewModel(container.historyRepository) }
                        },
                    )
                    HistoryScreen(
                        viewModel = vm,
                        onResume = { entry: HistoryEntry ->
                            scope.launch {
                                draftFile = container.lyricFileRepository.loadDraft(entry.filePath, entry.format)
                                draftPath = entry.filePath
                                historyId = entry.id
                                navController.navigate(Screen.EDIT)
                            }
                        },
                        onDelete = { entry -> vm.delete(entry) },
                    )
                }

                composable(Screen.SETTINGS) {
                    val vm: SettingsViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { SettingsViewModel(container.settingsRepository) }
                        },
                    )
                    SettingsScreen(viewModel = vm)
                }

                composable(Screen.REMOVE_TIMESTAMP) {
                    val vm: RemoveTimestampViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { RemoveTimestampViewModel(container.lyricFileRepository) }
                        },
                    )
                    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                        if (uri != null) {
                            val fileName = queryFileNameBlocking(context, uri)
                            vm.loadFromUri(uri, fileName)
                        }
                    }
                    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
                        if (uri != null) scope.launch { vm.saveAsTextFile(uri) }
                    }
                    LaunchedEffect(Unit) { importLauncher.launch(arrayOf("*/*")) }
                    RemoveTimestampScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onSaveAsText = { saveLauncher.launch("lyrics.txt") },
                    )
                }

                composable(
                    route = Screen.CREATE_INPUT,
                    arguments = listOf(navArgument(Screen.ARG_FORMAT) {}),
                ) { entry: NavBackStackEntry ->
                    val format = FileFormat.valueOf(
                        entry.arguments?.getString(Screen.ARG_FORMAT) ?: FileFormat.SDLRC.name,
                    )
                    val vm: CreateInputViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { CreateInputViewModel(format, container.mediaSessionMonitor) }
                        },
                    )
                    CreateInputScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onProceedToEdit = {
                            draftFile = vm.buildLyricFile()
                            draftPath = null
                            navController.navigate(Screen.EDIT)
                        },
                    )
                }

                composable(Screen.EDIT) {
                    val initialFile = draftFile ?: LyricFile(title = "", format = FileFormat.SDLRC)
                    val vm: EditViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                EditViewModel(
                                    initialFile = initialFile,
                                    draftPath = draftPath,
                                    historyEntryId = historyId,
                                    lyricFileRepository = container.lyricFileRepository,
                                    historyRepository = container.historyRepository,
                                    settingsRepository = container.settingsRepository,
                                    mediaSessionMonitor = container.mediaSessionMonitor,
                                )
                            }
                        },
                    )
                    EditScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onProceedToOutput = {
                            draftFile = vm.lyricFile.value
                            draftPath = vm.currentDraftPath()
                            navController.navigate(Screen.PREVIEW)
                        },
                    )
                }

                composable(Screen.PREVIEW) {
                    val file = draftFile ?: LyricFile(title = "", format = FileFormat.SDLRC)
                    val vm: PreviewViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { PreviewViewModel(file, container.mediaSessionMonitor) }
                        },
                    )
                    val extension = file.format.extension
                    // text/plain 等の「拡張子が決まっているMIMEタイプ」を渡すと、
                    // 一部のドキュメントプロバイダがファイル名の拡張子を .txt に強制的に
                    // 書き換えてしまうことがある。.sdlrc / .lrc という独自拡張子を維持するため、
                    // ここでは汎用の application/octet-stream を指定し、ファイル名(.sdlrc/.lrc)を
                    // そのまま尊重させるようにしている。
                    val mimeType = "application/octet-stream"
                    val exportLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument(mimeType),
                    ) { uri: Uri? ->
                        if (uri != null) {
                            scope.launch {
                                container.lyricFileRepository.exportLyricFile(uri, file)
                                navController.navigate(Screen.HOME) {
                                    popUpTo(Screen.HOME) { inclusive = true }
                                }
                            }
                        }
                    }
                    PreviewScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onExport = {
                            val safeTitle = file.title.ifBlank { "lyrics" }
                            exportLauncher.launch("$safeTitle.$extension")
                        },
                    )
                }
            }
        }
    }
}

/** SAFで選択したUriの表示ファイル名を取得する */
private suspend fun queryFileName(context: android.content.Context, uri: Uri): String? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { queryFileNameBlocking(context, uri) }

private fun queryFileNameBlocking(context: android.content.Context, uri: Uri): String {
    var name = "imported"
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) name = it.getString(nameIndex) ?: name
    }
    return name
}