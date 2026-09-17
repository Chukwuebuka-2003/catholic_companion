package org.catholiccompanion.app.ui.screens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnMarkdownTest {
    @Test
    fun `markdown answer separates headings paragraphs and nested lists`() {
        val blocks = parseMarkdownBlocks(
            """
            ## Scripture

            Christ reveals the Father's love **through the Cross** [S1].

            1. **First Reading:** Numbers 21
              - The people look upon the bronze serpent.
            """.trimIndent(),
        )

        assertEquals(4, blocks.size)
        assertEquals(MarkdownBlock.Heading(2, "Scripture"), blocks[0])
        assertTrue(blocks[1] is MarkdownBlock.Paragraph)
        assertEquals("1.", (blocks[2] as MarkdownBlock.ListItem).marker)
        assertEquals(1, (blocks[3] as MarkdownBlock.ListItem).depth)
    }

    @Test
    fun `inline markdown renders formatting without visible markers`() {
        val value = markdownInlineText(
            "This is **important**, *reflective*, and sourced [S2].",
            Color.Magenta,
        )

        assertEquals("This is important, reflective, and sourced [S2].", value.text)
        assertFalse(value.text.contains("**"))
        assertTrue(value.spanStyles.size >= 3)
    }
}
