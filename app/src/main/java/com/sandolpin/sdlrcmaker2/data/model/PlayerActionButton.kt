package com.sandolpin.sdlrcmaker2.data.model

import kotlinx.serialization.Serializable

/**
 * 編集画面のメディアプレイヤー下に表示する打刻用アクションボタン。
 * 設定画面でどれを表示するか選べる(sdlrcのみ対象。lrcは開始のみのため常に非表示)。
 */
@Serializable
enum class PlayerActionButton(val label: String) {
    FINISH_AND_START_NEXT("現在の行を終了し、次の行を開始"),
    FINISH_CURRENT("現在の行を終了"),
    START_NEXT("次の行を開始"),
}
