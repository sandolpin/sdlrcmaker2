package com.sandolpin.sdlrcmaker2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    outline = LightOutline,
)

private val DarkColors = darkColorScheme(
    primary = BlueSecondary,
    secondary = BluePrimary,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    outline = DarkOutline,
)

/**
 * アプリ全体のテーマ。
 * darkModeOption が SYSTEM の場合のみ端末設定(isSystemInDarkTheme)に従う。
 * ON/OFFの場合はそれを強制的に優先する。
 *
 * また、Android 12(API 31)以降では Dynamic Color(Material You)に対応しており、
 * 端末の壁紙などから生成される配色を自動的に採用する。API 30以下では
 * Dynamic Colorが存在しないため、このファイルで定義した固定の配色(青系)にフォールバックする。
 */
@Composable
fun SdlrcMaker2Theme(
    darkModeOption: DarkModeOption = DarkModeOption.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (darkModeOption) {
        DarkModeOption.SYSTEM -> isSystemInDarkTheme()
        DarkModeOption.ON -> true
        DarkModeOption.OFF -> false
    }

    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        supportsDynamicColor && useDarkTheme -> dynamicDarkColorScheme(context)
        supportsDynamicColor && !useDarkTheme -> dynamicLightColorScheme(context)
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SdlrcTypography,
        shapes = SdlrcShapes,
        content = content,
    )
}