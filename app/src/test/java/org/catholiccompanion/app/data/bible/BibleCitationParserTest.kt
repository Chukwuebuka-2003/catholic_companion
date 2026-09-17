package org.catholiccompanion.app.data.bible

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleCitationParserTest {
    @Test
    fun `parses cross chapter and partial verse references`() {
        val option = BibleCitationParser.parse("1 John 1:5 – 2:2").single()

        assertEquals("1JO", option.book.code)
        assertEquals(BibleVerseRange(1, 5, 2, 2), option.ranges.single())
        assertFalse(option.usesPartialVerses)

        val partial = BibleCitationParser.parse("Isaiah 45:6c-8, 18, 21c-25").single()
        assertTrue(partial.usesPartialVerses)
        assertEquals(3, partial.ranges.size)
    }

    @Test
    fun `parses alternatives and inherited book chapters`() {
        val alternatives = BibleCitationParser.parse("2 Timothy 1:1-8|Titus 1:1-5")
        assertEquals(listOf("2TI", "TIT"), alternatives.map { it.book.code })

        val option = BibleCitationParser.parse("2 Timothy 2:8-13; 3:10-12").single()
        assertEquals(
            listOf(BibleVerseRange(2, 8, 2, 13), BibleVerseRange(3, 10, 3, 12)),
            option.ranges,
        )
    }

    @Test
    fun `supports calendar source aliases and whole psalms`() {
        assertEquals("MAR", BibleCitationParser.parse("Marc 16:15-20").single().book.code)
        assertEquals("MAT", BibleCitationParser.parse("Mattthew 24:42a,44").single().book.code)
        assertEquals("2MA", BibleCitationParser.parse("Maccabees 7:1-2,9-14").single().book.code)
        assertEquals(
            BibleVerseRange(103, null, 103, null),
            BibleCitationParser.parse("Psalm 103 (102)").single().ranges.single(),
        )
        assertEquals(
            BibleVerseRange(1, 7, 1, 20),
            BibleCitationParser.parse("Philemon 7-20").single().ranges.single(),
        )
        assertEquals(
            BibleVerseRange(1, 4, 1, 9),
            BibleCitationParser.parse("2 John 4-9").single().ranges.single(),
        )
    }

    @Test
    fun `does not invent a passage for a common reference`() {
        assertTrue(
            BibleCitationParser.parse("From the Common of the Blessed Virgin Mary").isEmpty(),
        )
    }

    @Test
    fun `every concrete bundled calendar citation maps to a bundled Bible book`() {
        val calendar = projectFile("app/src/main/assets/calendars/general-roman-2026.json")
        val citations = Regex(""""citation"\s*:\s*"([^"]+)"""")
            .findAll(calendar.readText())
            .map { match -> match.groupValues[1] }
            .toList()
        val unresolved = citations.filter { citation ->
            !citation.startsWith("From the Common") &&
                BibleCitationParser.parse(citation).isEmpty()
        }
        assertTrue("Unresolved citations: $unresolved", unresolved.isEmpty())

        assertEquals(365, Regex(""""label"\s*:\s*"First reading"""").findAll(calendar.readText()).count())
        assertEquals(365, Regex(""""label"\s*:\s*"Responsorial psalm"""").findAll(calendar.readText()).count())
        assertEquals(365, Regex(""""label"\s*:\s*"Gospel"""").findAll(calendar.readText()).count())

        val usedBookCodes = citations.flatMap(BibleCitationParser::parse).map { it.book.code }.toSet()
        val missingAssets = usedBookCodes.filter { code ->
            !projectFile("app/src/main/assets/bible/web-c/${code.lowercase()}.vpl.txt").isFile
        }
        assertTrue("Missing Bible assets: $missingAssets", missingAssets.isEmpty())

        val verseKeys = usedBookCodes.associateWith { code ->
            projectFile("app/src/main/assets/bible/web-c/${code.lowercase()}.vpl.txt")
                .readLines()
                .mapNotNull { line ->
                    line.substringBefore('\t').split(':').takeIf { it.size == 2 }?.let { parts ->
                        val chapter = parts[0].toIntOrNull()
                        val verse = parts[1].toIntOrNull()
                        if (chapter != null && verse != null) chapter to verse else null
                    }
                }
                .toSet()
        }
        val emptyRanges = citations.flatMap(BibleCitationParser::parse).flatMap { option ->
            option.ranges.mapNotNull { range ->
                val resolves = verseKeys.getValue(option.book.code).any { (chapter, verse) ->
                    val point = chapter to verse
                    val start = range.startChapter to (range.startVerse ?: Int.MIN_VALUE)
                    val end = range.endChapter to (range.endVerse ?: Int.MAX_VALUE)
                    comparePoints(point, start) >= 0 && comparePoints(point, end) <= 0
                }
                if (resolves) null else "${option.citation}: $range"
            }
        }
        assertTrue("Citation ranges with no bundled verses: $emptyRanges", emptyRanges.isEmpty())
    }

    private fun comparePoints(left: Pair<Int, Int>, right: Pair<Int, Int>): Int =
        compareValuesBy(left, right, Pair<Int, Int>::first, Pair<Int, Int>::second)

    private fun projectFile(path: String): File {
        val fromRoot = File(path)
        if (fromRoot.exists()) return fromRoot
        return File("../$path")
    }
}
