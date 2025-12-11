package com.example.yoomusic.data

object LrcParser {

    // Example line: [00:17.12] I feel your breath upon my neck
    private val lineRegex =
        """\[(\d{2}):(\d{2})(?:\.(\d{2}))?] ?(.*)""".toRegex()

    fun parse(lrc: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()

        lrc.lines().forEach { line ->
            val match = lineRegex.find(line) ?: return@forEach

            val (min, sec, centi, text) = match.destructured

            val ms =
                min.toLong() * 60_000 +
                        sec.toLong() * 1_000 +
                        (centi.toLongOrNull() ?: 0) * 10

            result.add(
                LyricLine(
                    timestampMs = ms,
                    text = text.trim()
                )
            )
        }

        return result.sortedBy { it.timestampMs }
    }
}
