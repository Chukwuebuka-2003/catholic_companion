package org.catholiccompanion.app.data.liturgy

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bundled calendar stores `rank` as free text imported from upstream, with inconsistent
 * casing. These cases are the exact strings present in general-roman-2026.json; if an import
 * introduces a new spelling it must be mapped here rather than silently becoming OTHER.
 */
class CelebrationRankTest {
    @Test
    fun `maps every rank spelling used by the bundled calendar`() {
        assertEquals(CelebrationRank.SOLEMNITY, CelebrationRank.from("SOLEMNITY"))
        assertEquals(CelebrationRank.FEAST, CelebrationRank.from("FEAST"))
        assertEquals(CelebrationRank.FEAST, CelebrationRank.from("FEAST OF THE LORD"))
        assertEquals(CelebrationRank.MEMORIAL, CelebrationRank.from("Memorial"))
        assertEquals(CelebrationRank.OPTIONAL_MEMORIAL, CelebrationRank.from("optional memorial"))
        assertEquals(CelebrationRank.COMMEMORATION, CelebrationRank.from("commemoration"))
        assertEquals(CelebrationRank.WEEKDAY, CelebrationRank.from("weekday"))
    }

    @Test
    fun `treats celebrations outranking solemnities as solemnities`() {
        assertEquals(
            CelebrationRank.SOLEMNITY,
            CelebrationRank.from("celebration with precedence over solemnities"),
        )
    }

    @Test
    fun `optional memorial is not confused with memorial`() {
        // "optional memorial" also contains "memorial", so ordering of the checks matters.
        assertEquals(CelebrationRank.OPTIONAL_MEMORIAL, CelebrationRank.from("Optional Memorial"))
        assertEquals(CelebrationRank.MEMORIAL, CelebrationRank.from("MEMORIAL"))
    }

    @Test
    fun `ignores surrounding whitespace and casing`() {
        assertEquals(CelebrationRank.SOLEMNITY, CelebrationRank.from("  solemnity  "))
    }

    @Test
    fun `unknown ranks fall back to OTHER rather than throwing`() {
        assertEquals(CelebrationRank.OTHER, CelebrationRank.from("unclassified"))
    }
}
