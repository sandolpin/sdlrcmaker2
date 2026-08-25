package com.sandolpin.sdlrcmaker2.data.parser

import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import com.sandolpin.sdlrcmaker2.util.TimeFormatter

/**
 * 標準lrc形式のパーサー。他の多くのアプリとの互換性を重視し、
 * 一般的なlrcタグ([ti:] [ar:])と行タイムスタンプ([mm:ss.xx])のみを扱う。
 * 終了時刻・右揃えの概念はない。
 *
 * 例:
 *   [ti:アイドル]
 *   [ar:すごいアイドル]
 *   [00:00.00]すっごいアイドル
 */
class LrcParser : LyricParser {

    companion object {
        private val LINE_REGEX = Regex("""^\[(\d{1,3}:\d{2}\.\d{2})](.*)$""")
        private val TITLE_TAG_REGEX = Regex("""^\[ti:(.*)]$""")
        private val ARTIST_TAG_REGEX = Regex("""^\[ar:(.*)]$""")
    }

    override fun parse(content: String): LyricFile {
        var title = ""
        var artist = ""
        val lines = mutableListOf<LyricLine>()

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trimEnd('\r')
            when {
                line.isBlank() -> Unit
                TITLE_TAG_REGEX.matches(line) ->
                    title = TITLE_TAG_REGEX.matchEntire(line)!!.groupValues[1]
                ARTIST_TAG_REGEX.matches(line) ->
                    artist = ARTIST_TAG_REGEX.matchEntire(line)!!.groupValues[1]
                else -> {
                    val match = LINE_REGEX.matchEntire(line)
                    if (match != null) {
                        val (startStr, text) = match.destructured
                        lines += LyricLine(text = text, startMs = TimeFormatter.parse(startStr))
                    }
                }
            }
        }

        return LyricFile(title = title, artist = artist, format = FileFormat.LRC, lines = lines)
    }

    override fun serialize(file: LyricFile): String = buildString {
        if (file.title.isNotBlank()) appendLine("[ti:${file.title}]")
        if (file.artist.isNotBlank()) appendLine("[ar:${file.artist}]")
        file.lines.forEach { line ->
            val start = TimeFormatter.format(line.startMs ?: 0L)
            appendLine("[$start]${line.text}")
        }
    }
}
