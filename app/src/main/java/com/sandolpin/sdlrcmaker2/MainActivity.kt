package com.sandolpin.sdlrcmaker2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sandolpin.sdlrcmaker2.ui.navigation.SdlrcNavHost
import com.sandolpin.sdlrcmaker2.ui.theme.DarkModeOption
import com.sandolpin.sdlrcmaker2.ui.theme.SdlrcMaker2Theme

/**
 * アプリの唯一のActivity。
 *
 * enableEdgeToEdge() を呼ぶことで、コンテンツがステータスバー/ナビゲーションバーの裏側まで
 * 描画されるようになる(いわゆる「エッジ・トゥ・エッジ」表示)。そのままだと画面の内容が
 * バーに隠れてしまうため、各画面側では WindowInsets(navigationBars など)を使って
 * 必要な余白を確保している(AppBottomBar.kt の windowInsetsPadding を参照)。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as SdlrcMakerApp).container

        setContent {
            val darkModeOption by container.settingsRepository.darkModeOption
                .collectAsState(initial = DarkModeOption.SYSTEM)

            SdlrcMaker2Theme(darkModeOption = darkModeOption) {
                SdlrcNavHost(container = container)
            }
        }
    }
}
