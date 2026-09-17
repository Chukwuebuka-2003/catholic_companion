package org.catholiccompanion.app.data.bible

import android.content.Context
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val WEB_C_TITLE = "World English Bible, Catholic Edition"
const val WEB_C_EDITION = "2020 stable text edition"
const val WEB_C_SOURCE_URL = "https://ebible.org/eng-web-c/"
const val WEB_C_COPYRIGHT_URL = "https://ebible.org/eng-web-c/copyright.htm"

data class BibleVerse(
    val chapter: Int,
    val verse: Int,
    val text: String,
)

data class ResolvedBibleSection(
    val citation: String,
    val verses: List<BibleVerse>,
)

data class ResolvedBiblePassage(
    val sections: List<ResolvedBibleSection>,
    val usesPartialVerses: Boolean,
    val hasAlternatives: Boolean,
)

class BibleRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private val bookCache = ConcurrentHashMap<String, List<BibleVerse>>()

    suspend fun resolve(citation: String): ResolvedBiblePassage? = withContext(Dispatchers.IO) {
        val options = BibleCitationParser.parse(citation)
        if (options.isEmpty()) return@withContext null
        val sections = options.mapNotNull { option ->
            val book = loadBook(option.book.code)
            val selected = linkedMapOf<Pair<Int, Int>, BibleVerse>()
            option.ranges.forEach { range ->
                book.asSequence()
                    .filter { verse -> verse.isWithin(range) }
                    .forEach { verse -> selected[verse.chapter to verse.verse] = verse }
            }
            selected.values.takeIf { it.isNotEmpty() }?.let { verses ->
                ResolvedBibleSection(option.citation, verses.toList())
            }
        }
        sections.takeIf { it.isNotEmpty() }?.let {
            ResolvedBiblePassage(
                sections = it,
                usesPartialVerses = options.any(ParsedBibleOption::usesPartialVerses),
                hasAlternatives = options.size > 1,
            )
        }
    }

    private fun loadBook(code: String): List<BibleVerse> = bookCache[code] ?: synchronized(bookCache) {
        bookCache[code] ?: assets.open("bible/web-c/${code.lowercase()}.vpl.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.mapNotNull { line ->
                    val tab = line.indexOf('\t')
                    val colon = line.indexOf(':')
                    if (tab <= colon || colon <= 0) return@mapNotNull null
                    val chapter = line.substring(0, colon).toIntOrNull() ?: return@mapNotNull null
                    val verse = line.substring(colon + 1, tab).toIntOrNull() ?: return@mapNotNull null
                    BibleVerse(chapter, verse, line.substring(tab + 1))
                }.toList()
            }.also { verses -> bookCache[code] = verses }
    }
}

private fun BibleVerse.isWithin(range: BibleVerseRange): Boolean {
    val point = chapter to verse
    val start = range.startChapter to (range.startVerse ?: Int.MIN_VALUE)
    val end = range.endChapter to (range.endVerse ?: Int.MAX_VALUE)
    return comparePoints(point, start) >= 0 && comparePoints(point, end) <= 0
}

private fun comparePoints(left: Pair<Int, Int>, right: Pair<Int, Int>): Int =
    compareValuesBy(left, right, Pair<Int, Int>::first, Pair<Int, Int>::second)
