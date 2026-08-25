package com.sandolpin.sdlrcmaker2.ui.navigation

/**
 * アプリ内の画面ルート定義。
 *
 * 作成フロー(入力→編集→プレビュー)は同一のLyricFileを扱い続ける必要があるが、
 * NavHost側でDIコンテナを介さない簡易ViewModel生成を採用しているため、
 * ここでは「編集中のLyricFile」をSdlrcNavHost内でremember保持し、
 * 各画面のViewModelを都度組み立てる方式にしている(詳細はSdlrcNavHost.ktのコメント参照)。
 */
object Screen {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val REMOVE_TIMESTAMP = "remove_timestamp"
    const val CREATE_INPUT = "create_input/{format}"
    const val EDIT = "edit"
    const val PREVIEW = "preview"

    const val ARG_FORMAT = "format"

    fun createInputRoute(format: String) = "create_input/$format"
}
