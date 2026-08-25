package com.sandolpin.sdlrcmaker2.data.model

import kotlinx.serialization.Serializable

/**
 * 作成するファイル形式。
 * SDLRC: 独自形式。開始/終了時刻・右揃え(/r)に対応。
 * LRC  : 標準lrc形式。開始時刻のみ。互換性重視。
 */
@Serializable
enum class FileFormat(val extension: String, val label: String) {
    SDLRC(extension = "sdlrc", label = "sdlrc"),
    LRC(extension = "lrc", label = "lrc");

    val supportsEndTime: Boolean get() = this == SDLRC
    val supportsRightAlign: Boolean get() = this == SDLRC
}
