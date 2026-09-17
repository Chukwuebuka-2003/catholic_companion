package org.catholiccompanion.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.catholiccompanion.app.ai.AiCitation
import java.net.URI
import org.catholiccompanion.app.ai.AiDayContext
import org.catholiccompanion.app.ai.AiMysteryContext
import org.catholiccompanion.app.ai.AiRequestContext
import org.catholiccompanion.app.ai.AiTask
import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.ui.LearnMessage
import org.catholiccompanion.app.ui.LearnMessageRole
import org.catholiccompanion.app.ui.LearnUiState
import org.catholiccompanion.app.ui.theme.CatholicCompanionTheme

@Composable
fun LearnScreen(
    contentPadding: PaddingValues,
    state: LearnUiState,
    dayContext: AiDayContext?,
    mysterySet: MysterySet,
    onDraftChange: (String) -> Unit,
    onAsk: (AiTask, String, AiRequestContext) -> Unit,
    onClear: () -> Unit,
) {
    var mysteryIndex by remember(mysterySet) { mutableIntStateOf(0) }
    val selectedMystery = mysterySet.mysteries[mysteryIndex]
    val mysteryContext = AiMysteryContext(
        mysterySet = mysterySet.displayName,
        title = selectedMystery.title,
        scriptureReference = selectedMystery.scriptureReference,
    )
    val requestContext = AiRequestContext(day = dayContext, mystery = mysteryContext)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ask with context.", style = MaterialTheme.typography.headlineLarge)
            if (state.messages.isNotEmpty()) {
                TextButton(onClick = onClear, enabled = !state.isLoading) { Text("Clear") }
            }
        }
        Text(
            "Ask about today’s liturgy or a Rosary mystery. Generated explanations are distinct from " +
                "Scripture and official Church teaching.",
            style = MaterialTheme.typography.bodyLarge,
        )

        if (!state.isConfigured) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FastAPI AI not connected", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "This build keeps AI disabled because no deployed HTTPS FastAPI URL was supplied. " +
                            "No API key is stored in the APK.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        Text("Start with context", style = MaterialTheme.typography.titleLarge)
        dayContext?.let { day ->
            OutlinedButton(
                onClick = {
                    onAsk(
                        AiTask.EXPLAIN_READING,
                        "Explain today’s readings and how they relate to ${day.celebration}.",
                        requestContext,
                    )
                },
                enabled = state.isConfigured && !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Explain today’s readings")
            }
        }

        Text("Choose a mystery", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            mysterySet.mysteries.forEachIndexed { index, mystery ->
                FilterChip(
                    selected = mysteryIndex == index,
                    onClick = { mysteryIndex = index },
                    label = { Text("${index + 1}. ${mystery.title.removePrefix("The ")}") },
                )
            }
        }
        OutlinedButton(
            onClick = {
                onAsk(
                    AiTask.EXPLAIN_MYSTERY,
                    "Explain ${selectedMystery.title} and help me contemplate it while praying.",
                    requestContext,
                )
            },
            enabled = state.isConfigured && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Explain ${selectedMystery.title}")
        }

        state.messages.forEach { message -> MessageCard(message) }

        if (state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text("Finding and checking sources…")
            }
        }
        state.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        OutlinedTextField(
            value = state.draft,
            onValueChange = onDraftChange,
            enabled = state.isConfigured && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5,
            label = { Text("Ask a follow-up") },
            supportingText = { Text("Do not include private intentions unless you choose to send them.") },
        )
        Button(
            onClick = { onAsk(AiTask.FOLLOW_UP, state.draft, requestContext) },
            enabled = state.isConfigured && !state.isLoading && state.draft.trim().length >= 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ask")
        }
        Text(
            "Your question and the displayed liturgical context may be sent to the configured " +
                "AI and web-search services when you ask. " +
                "Conversation history is kept only in this app session.",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MessageCard(message: LearnMessage) {
    if (message.role == LearnMessageRole.ASSISTANT) {
        AssistantMessageCard(message)
    } else {
        UserMessageCard(message)
    }
}

@Composable
private fun UserMessageCard(message: LearnMessage) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    "Your question",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                )
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun AssistantMessageCard(message: LearnMessage) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "GENERATED EXPLANATION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            MarkdownAnswer(message.text)
            if (message.citations.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "Sources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                message.citations.distinctBy { it.url }.forEachIndexed { index, citation ->
                    Card(
                        onClick = { uriHandler.openUri(citation.url) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                "SOURCE ${index + 1} · ${sourceHost(citation.url)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                citation.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "Open official source",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            message.limitations.forEach { limitation ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        limitation,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownAnswer(markdown: String) {
    val accentColor = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        parseMarkdownBlocks(markdown).forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = markdownInlineText(block.text, accentColor),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.SemiBold,
                )
                is MarkdownBlock.Paragraph -> Text(
                    text = markdownInlineText(block.text, accentColor),
                    style = MaterialTheme.typography.bodyLarge,
                )
                is MarkdownBlock.ListItem -> Row(
                    modifier = Modifier.padding(start = (block.depth * 14).dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = block.marker,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = markdownInlineText(block.text, accentColor),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

internal sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(
        val marker: String,
        val text: String,
        val depth: Int,
    ) : MarkdownBlock
}

internal fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraph.joinToString(" "))
            paragraph.clear()
        }
    }

    markdown.replace("\r\n", "\n").lineSequence().forEach { rawLine ->
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty()) {
            flushParagraph()
            return@forEach
        }

        val heading = HEADING_PATTERN.matchEntire(trimmed)
        val listItem = LIST_ITEM_PATTERN.matchEntire(rawLine)
        when {
            heading != null -> {
                flushParagraph()
                blocks += MarkdownBlock.Heading(
                    level = heading.groupValues[1].length.coerceAtMost(3),
                    text = heading.groupValues[2],
                )
            }
            listItem != null -> {
                flushParagraph()
                val indentation = listItem.groupValues[1].replace("\t", "    ").length
                val token = listItem.groupValues[2]
                blocks += MarkdownBlock.ListItem(
                    marker = if (token.first().isDigit()) token else "•",
                    text = listItem.groupValues[3],
                    depth = (indentation / 2).coerceIn(0, 2),
                )
            }
            else -> paragraph += trimmed
        }
    }
    flushParagraph()
    return blocks
}

internal fun markdownInlineText(text: String, accentColor: Color): AnnotatedString =
    buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            when {
                text.startsWith("**", cursor) -> {
                    val end = text.indexOf("**", cursor + 2)
                    if (end >= 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(cursor + 2, end))
                        }
                        cursor = end + 2
                    } else {
                        append(text[cursor])
                        cursor += 1
                    }
                }
                text[cursor] == '*' -> {
                    val end = text.indexOf('*', cursor + 1)
                    if (end >= 0) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(cursor + 1, end))
                        }
                        cursor = end + 1
                    } else {
                        append(text[cursor])
                        cursor += 1
                    }
                }
                text[cursor] == '[' -> {
                    val match = SOURCE_MARKER_PATTERN.find(text, cursor)
                    if (match?.range?.first == cursor) {
                        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                            append(match.value)
                        }
                        cursor = match.range.last + 1
                    } else {
                        append(text[cursor])
                        cursor += 1
                    }
                }
                else -> {
                    append(text[cursor])
                    cursor += 1
                }
            }
        }
    }

private fun sourceHost(url: String): String =
    runCatching { URI(url).host?.removePrefix("www.") }
        .getOrNull()
        .orEmpty()
        .ifBlank { "official source" }

private val HEADING_PATTERN = Regex("^(#{1,3})\\s+(.+)$")
private val LIST_ITEM_PATTERN = Regex("^(\\s*)([-*+]|\\d+[.)])\\s+(.+)$")
private val SOURCE_MARKER_PATTERN = Regex("\\[S[1-9]\\d*]")

@Preview(showBackground = true)
@Composable
private fun LearnScreenPreview() {
    CatholicCompanionTheme {
        Surface {
            LearnScreen(
                contentPadding = PaddingValues(0.dp),
                state = LearnUiState(
                    isConfigured = true,
                    draft = "",
                    messages = listOf(
                        LearnMessage(
                            role = LearnMessageRole.USER,
                            text = "What is the spiritual meaning of the Resurrection?",
                        ),
                        LearnMessage(
                            role = LearnMessageRole.ASSISTANT,
                            text = "The Resurrection of Jesus Christ is the central pillar of Catholic faith and hope.\n\n### Key Theological Reflections\n* **Victory over Death**: Through His Resurrection, Christ conquered sin and eternal death.\n* **Hope of Eternal Life**: By rising from the dead, Jesus opens heaven to believers.",
                            citations = listOf(
                                AiCitation(
                                    title = "Catechism of the Catholic Church §638",
                                    url = "https://www.vatican.va/archive/ENG0015/_P1V.HTM",
                                    startIndex = 0,
                                    endIndex = 0,
                                ),
                            ),
                        ),
                    ),
                ),
                dayContext = null,
                mysterySet = MysterySet.GLORIOUS,
                onDraftChange = {},
                onAsk = { _, _, _ -> },
                onClear = {},
            )
        }
    }
}

