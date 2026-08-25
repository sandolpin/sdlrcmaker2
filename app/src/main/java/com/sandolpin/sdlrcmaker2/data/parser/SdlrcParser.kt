package com.sandolpin.sdlrcmaker2.data.parser

import com.sandolpin.sdlrcmaker2.data.model.FileFormat
import com.sandolpin.sdlrcmaker2.data.model.LyricFile
import com.sandolpin.sdlrcmaker2.data.model.LyricLine
import com.sandolpin.sdlrcmaker2.util.TimeFormatter

/**
 * sdlrc形式のパーサー。
 *
 * ルール:
 *   title=タイトル名
 *   artist=アーティスト名
 *   [開始時刻][/終了時刻]歌詞
 *   [開始時刻 /r][/終了時刻]右揃えで歌詞を表示
 *
 * 例:
 *   title=アイドル
 *   artist=すごいアイドル
 *   [00:00.00][/00:03.50]すっごいアイドル
 *   [00:02.10 /r][/00:03.50](Woo～)
 */
class SdlrcParser : LyricParser {

    companion object {
        // 前後に余分な空白が入っていても許容する(手動編集されたファイル等への耐性)。
        // /r の前の空白は0個・1個どちらでも読み込めるようにしてある(書き出しは常に1個)。
        private val LINE_REGEX = Regex(
            """^\s*\[\s*(\d{1,3}:\d{2}\.\d{2})\s*(/r)?\s*]\s*\[\s*/\s*(\d{1,3}:\d{2}\.\d{2})\s*]\s*(.*)$""",
        )
        private val START_ONLY_REGEX = Regex(
            """^\s*\[\s*(\d{1,3}:\d{2}\.\d{2})\s*(/r)?\s*]\s*(.*)$""",
        )
        private const val TITLE_PREFIX = "title="
        private const val ARTIST_PREFIX = "artist="
    }

    override fun parse(content: String): LyricFile {
        var title = ""
        var artist = ""
        val lines = mutableListOf<LyricLine>()

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trimEnd('\r')
            when {
                line.startsWith(TITLE_PREFIX) -> title = line.removePrefix(TITLE_PREFIX)
                line.startsWith(ARTIST_PREFIX) -> artist = line.removePrefix(ARTIST_PREFIX)
                line.isBlank() -> Unit
                else -> lines += parseLine(line)
            }
        }

        return LyricFile(title = title, artist = artist, format = FileFormat.SDLRC, lines = lines)
    }

    private fun parseLine(line: String): LyricLine {
        LINE_REGEX.matchEntire(line)?.let { match ->
            val (startStr, rFlag, endStr, text) = match.destructured
            return LyricLine(
                text = text,
                startMs = TimeFormatter.parse(startStr),
                endMs = TimeFormatter.parse(endStr),
                isRightAlign = rFlag == "/r",
            )
        }
        START_ONLY_REGEX.matchEntire(line)?.let { match ->
            val (startStr, rFlag, text) = match.destructured
            return LyricLine(
                text = text,
                startMs = TimeFormatter.parse(startStr),
                isRightAlign = rFlag == "/r",
            )
        }
        return LyricLine(text = line)
    }

    override fun serialize(file: LyricFile): String = buildString {
        appendLine("$TITLE_PREFIX${file.title}")
        appendLine("$ARTIST_PREFIX${file.artist}")
        file.lines.forEach { line ->
            val start = TimeFormatter.format(line.startMs ?: 0L)
            val end = TimeFormatter.format(line.endMs ?: (line.startMs ?: 0L))
            val rFlag = if (line.isRightAlign) " /r" else ""
            appendLine("[$start$rFlag][/$end]${line.text}")
        }
    }
}