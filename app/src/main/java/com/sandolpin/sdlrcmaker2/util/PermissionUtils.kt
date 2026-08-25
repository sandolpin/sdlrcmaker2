package com.sandolpin.sdlrcmaker2.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * 「通知へのアクセス」権限(Notification Listener)関連のユーティリティ。
 * この権限は他の危険な権限と異なり、runtime permission dialogではなく
 * 設定アプリの専用画面からユーザーが手動で許可する必要がある。
 */
object PermissionUtils {

    /** 通知アクセスが現在このアプリに許可されているか */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }

    /** 通知アクセスの許可設定画面を開くIntent */
    fun notificationAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
