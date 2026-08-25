package com.sandolpin.sdlrcmaker2.data.parser

import com.sandolpin.sdlrcmaker2.data.model.LyricFile

/**
 * 歌詞ファイルのテキスト ⇔ LyricFile 変換を担うパーサーの共通interface。
 * SdlrcParser / LrcParser がそれぞれの形式ルールに従って実装する。
 */
interface LyricParser {

    /** ファイルの生テキストを解析してLyricFileに変換する */
    fun parse(content: String): LyricFile

    /** LyricFileをファイルの生テキストに変換する(エクスポート用) */
    fun serialize(file: LyricFile): String
}
