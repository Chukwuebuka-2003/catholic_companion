package org.catholiccompanion.app.ai

enum class AiTask(val wireName: String) {
    EXPLAIN_READING("explain_reading"),
    EXPLAIN_MYSTERY("explain_mystery"),
    FOLLOW_UP("follow_up"),
}

data class AiCitation(
    val title: String,
    val url: String,
    val startIndex: Int,
    val endIndex: Int,
)

data class AiAnswer(
    val text: String,
    val citations: List<AiCitation>,
    val limitations: List<String>,
)

data class AiHistoryItem(
    val role: String,
    val text: String,
)

data class AiDayContext(
    val date: String,
    val calendar: String,
    val celebration: String,
    val rank: String,
    val season: String,
    val readingReferences: List<String>,
)

data class AiMysteryContext(
    val mysterySet: String,
    val title: String,
    val scriptureReference: String,
)

data class AiRequestContext(
    val day: AiDayContext? = null,
    val mystery: AiMysteryContext? = null,
)

