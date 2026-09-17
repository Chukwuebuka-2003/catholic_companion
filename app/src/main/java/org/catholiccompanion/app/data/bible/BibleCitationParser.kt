package org.catholiccompanion.app.data.bible

data class BibleBook(
    val code: String,
    val name: String,
    val aliases: Set<String> = emptySet(),
)

data class BibleVerseRange(
    val startChapter: Int,
    val startVerse: Int?,
    val endChapter: Int = startChapter,
    val endVerse: Int? = startVerse,
)

data class ParsedBibleOption(
    val citation: String,
    val book: BibleBook,
    val ranges: List<BibleVerseRange>,
    val usesPartialVerses: Boolean,
)

object BibleBookCatalog {
    val books = listOf(
        BibleBook("GEN", "Genesis"),
        BibleBook("EXO", "Exodus"),
        BibleBook("LEV", "Leviticus"),
        BibleBook("NUM", "Numbers"),
        BibleBook("DEU", "Deuteronomy"),
        BibleBook("JOS", "Joshua"),
        BibleBook("JDG", "Judges"),
        BibleBook("RUT", "Ruth"),
        BibleBook("1SA", "1 Samuel"),
        BibleBook("2SA", "2 Samuel"),
        BibleBook("1KI", "1 Kings"),
        BibleBook("2KI", "2 Kings"),
        BibleBook("1CH", "1 Chronicles"),
        BibleBook("2CH", "2 Chronicles"),
        BibleBook("EZR", "Ezra"),
        BibleBook("NEH", "Nehemiah"),
        BibleBook("TOB", "Tobit"),
        BibleBook("JDT", "Judith"),
        BibleBook("ESG", "Esther (Greek)", setOf("Esther")),
        BibleBook("1MA", "1 Maccabees"),
        BibleBook("2MA", "2 Maccabees", setOf("Maccabees")),
        BibleBook("JOB", "Job"),
        BibleBook("PSA", "Psalms", setOf("Psalm")),
        BibleBook("PRO", "Proverbs", setOf("Proverb", "Proverbe")),
        BibleBook("ECC", "Ecclesiastes"),
        BibleBook("SOL", "Song of Solomon", setOf("Song of Songs")),
        BibleBook("WIS", "Wisdom"),
        BibleBook("SIR", "Sirach", setOf("Ecclesiasticus")),
        BibleBook("ISA", "Isaiah"),
        BibleBook("JER", "Jeremiah"),
        BibleBook("LAM", "Lamentations"),
        BibleBook("BAR", "Baruch"),
        BibleBook("EZE", "Ezekiel"),
        BibleBook("DNG", "Daniel (Greek)", setOf("Daniel")),
        BibleBook("HOS", "Hosea"),
        BibleBook("JOE", "Joel"),
        BibleBook("AMO", "Amos"),
        BibleBook("OBA", "Obadiah"),
        BibleBook("JON", "Jonah"),
        BibleBook("MIC", "Micah"),
        BibleBook("NAH", "Nahum"),
        BibleBook("HAB", "Habakkuk"),
        BibleBook("ZEP", "Zephaniah"),
        BibleBook("HAG", "Haggai"),
        BibleBook("ZEC", "Zechariah"),
        BibleBook("MAL", "Malachi"),
        BibleBook("MAT", "Matthew", setOf("Mattthew")),
        BibleBook("MAR", "Mark", setOf("Marc")),
        BibleBook("LUK", "Luke"),
        BibleBook("JOH", "John"),
        BibleBook("ACT", "Acts"),
        BibleBook("ROM", "Romans"),
        BibleBook("1CO", "1 Corinthians"),
        BibleBook("2CO", "2 Corinthians"),
        BibleBook("GAL", "Galatians"),
        BibleBook("EPH", "Ephesians"),
        BibleBook("PHI", "Philippians"),
        BibleBook("COL", "Colossians"),
        BibleBook("1TH", "1 Thessalonians"),
        BibleBook("2TH", "2 Thessalonians"),
        BibleBook("1TI", "1 Timothy"),
        BibleBook("2TI", "2 Timothy"),
        BibleBook("TIT", "Titus"),
        BibleBook("PHM", "Philemon"),
        BibleBook("HEB", "Hebrews", setOf("Hewbrews")),
        BibleBook("JAM", "James"),
        BibleBook("1PE", "1 Peter", setOf("1 Pt")),
        BibleBook("2PE", "2 Peter"),
        BibleBook("1JO", "1 John"),
        BibleBook("2JO", "2 John"),
        BibleBook("3JO", "3 John"),
        BibleBook("JUD", "Jude"),
        BibleBook("REV", "Revelation"),
    )

    internal val aliases = books.flatMap { book ->
        (book.aliases + book.name).map { alias -> alias to book }
    }.sortedByDescending { (alias, _) -> alias.length }
}

object BibleCitationParser {
    private val chapterAndVerses = Regex("""^(\d+)\s*:\s*(.+)$""")
    private val mistakenComma = Regex("""^(\d+)\s*,\s*(\d+[a-e]?(?:\s*-.*)?)$""", RegexOption.IGNORE_CASE)
    private val verseToken = Regex("""^(?:(\d+)\s*:\s*)?(\d+)([a-e]*)$""", RegexOption.IGNORE_CASE)
    private val singleChapterBooks = setOf("OBA", "PHM", "2JO", "3JO", "JUD")

    fun parse(citation: String): List<ParsedBibleOption> {
        val normalised = citation
            .trim()
            .removePrefix("Cf. ")
            .replace('–', '-')
            .replace('—', '-')
        if (normalised.startsWith("From the Common", ignoreCase = true)) return emptyList()
        return normalised.split('|').mapNotNull(::parseOption)
    }

    private fun parseOption(rawOption: String): ParsedBibleOption? {
        val option = rawOption.trim()
        val match = BibleBookCatalog.aliases.firstNotNullOfOrNull { (alias, book) ->
            if (option.startsWith(alias, ignoreCase = true) &&
                option.getOrNull(alias.length)?.isWhitespace() == true
            ) {
                book to option.drop(alias.length).trim()
            } else {
                null
            }
        } ?: return null
        val (book, rawBody) = match
        var body = rawBody.replace(Regex("""\s*\(\d+\)"""), "").trim()
        if (book.code in singleChapterBooks && ':' !in body) {
            body = "1:$body"
        } else {
            mistakenComma.matchEntire(body)?.let { commaMatch ->
                body = "${commaMatch.groupValues[1]}:${commaMatch.groupValues[2]}"
            }
        }

        var currentChapter: Int? = null
        var usesPartialVerses = false
        val ranges = buildList {
            body.split(';').forEach { rawSegment ->
                val segment = rawSegment.trim()
                val chapterMatch = chapterAndVerses.matchEntire(segment)
                if (chapterMatch == null) {
                    segment.toIntOrNull()?.let { chapter ->
                        currentChapter = chapter
                        add(BibleVerseRange(chapter, null, chapter, null))
                    }
                    return@forEach
                }
                val chapter = chapterMatch.groupValues[1].toInt()
                currentChapter = chapter
                chapterMatch.groupValues[2].split(',').forEach { rawPart ->
                    val part = rawPart.trim()
                    val dashIndex = part.indexOf('-')
                    val startRaw = if (dashIndex >= 0) part.substring(0, dashIndex) else part
                    val endRaw = if (dashIndex >= 0) part.substring(dashIndex + 1) else startRaw
                    val start = parseVerseToken(startRaw, currentChapter) ?: return@forEach
                    val end = parseVerseToken(endRaw, start.first) ?: return@forEach
                    usesPartialVerses = usesPartialVerses || start.third || end.third
                    add(BibleVerseRange(start.first, start.second, end.first, end.second))
                }
            }
        }
        if (ranges.isEmpty()) return null
        return ParsedBibleOption(option, book, ranges, usesPartialVerses)
    }

    private fun parseVerseToken(raw: String, inheritedChapter: Int): Triple<Int, Int, Boolean>? {
        val match = verseToken.matchEntire(raw.trim()) ?: return null
        val chapter = match.groupValues[1].toIntOrNull() ?: inheritedChapter
        val verse = match.groupValues[2].toInt()
        return Triple(chapter, verse, match.groupValues[3].isNotEmpty())
    }
}
