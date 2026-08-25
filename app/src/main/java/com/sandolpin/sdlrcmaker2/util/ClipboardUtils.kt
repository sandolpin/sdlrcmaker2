package com.sandolpin.sdlrcmaker2.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/** 「タイムスタンプを削除」画面のプレーンテキストコピーなどで使用する */
object ClipboardUtils {
    fun copyPlainText(context: Context, label: String, text: String) {
        val manager = context.getSystemService(ClipboardManager::class.java)
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
