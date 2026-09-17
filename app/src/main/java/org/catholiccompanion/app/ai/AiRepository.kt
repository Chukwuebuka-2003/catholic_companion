package org.catholiccompanion.app.ai

import android.content.Context
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.catholiccompanion.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

class AiServiceException(message: String) : IOException(message)

class AiRepository(
    context: Context,
    private val baseUrl: String = BuildConfig.AI_BASE_URL.trimEnd('/'),
) {
    private val preferences = context.getSharedPreferences("ai_installation", Context.MODE_PRIVATE)

    val isConfigured: Boolean
        get() = baseUrl.startsWith("https://")

    suspend fun ask(
        task: AiTask,
        question: String,
        context: AiRequestContext,
        history: List<AiHistoryItem>,
    ): AiAnswer = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            throw AiServiceException("The AI service is not connected in this build.")
        }
        val payload = JSONObject()
            .put("task", task.wireName)
            .put("question", question)
            .put("context", context.toJson())
            .put(
                "history",
                JSONArray(history.takeLast(6).map { item ->
                    JSONObject().put("role", item.role).put("text", item.text.take(1_200))
                }),
            )
            .toString()
            .encodeToByteArray()
        if (payload.size > MAX_REQUEST_BYTES) throw AiServiceException("This question contains too much context.")

        val connection = URL("$baseUrl/v1/ai/ask").openConnection() as? HttpsURLConnection
            ?: throw AiServiceException("The AI service must use HTTPS.")
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 12_000
            connection.readTimeout = 38_000
            connection.doOutput = true
            connection.setFixedLengthStreamingMode(payload.size)
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("X-Installation-Id", installationId())
            connection.outputStream.use { it.write(payload) }

            val status = connection.responseCode
            val responseBytes = readLimited(
                if (status in 200..299) connection.inputStream else connection.errorStream,
                MAX_RESPONSE_BYTES,
            )
            val response = runCatching { JSONObject(responseBytes.decodeToString()) }.getOrNull()
            if (status !in 200..299) {
                val serverMessage = response?.optString("message")?.takeIf { it.isNotBlank() }
                throw AiServiceException(
                    serverMessage ?: when (status) {
                        429 -> "Please wait before asking again."
                        HttpURLConnection.HTTP_UNAVAILABLE -> "The explanation service is temporarily unavailable."
                        else -> "The explanation could not be loaded."
                    },
                )
            }
            response?.toAnswer() ?: throw AiServiceException("The explanation service returned an invalid response.")
        } catch (error: AiServiceException) {
            throw error
        } catch (_: IOException) {
            throw AiServiceException("Check your connection and try again.")
        } finally {
            connection.disconnect()
        }
    }

    private fun installationId(): String {
        preferences.getString(INSTALLATION_ID, null)?.let { return it }
        val created = UUID.randomUUID().toString()
        preferences.edit().putString(INSTALLATION_ID, created).apply()
        return created
    }

    private companion object {
        const val INSTALLATION_ID = "installation_id"
        const val MAX_REQUEST_BYTES = 16_384
        const val MAX_RESPONSE_BYTES = 262_144
    }
}

private fun AiRequestContext.toJson(): JSONObject = JSONObject().apply {
    day?.let { value ->
        put(
            "day",
            JSONObject()
                .put("date", value.date)
                .put("calendar", value.calendar)
                .put("celebration", value.celebration)
                .put("rank", value.rank)
                .put("season", value.season)
                .put("readingReferences", JSONArray(value.readingReferences)),
        )
    }
    mystery?.let { value ->
        put(
            "mystery",
            JSONObject()
                .put("mysterySet", value.mysterySet)
                .put("title", value.title)
                .put("scriptureReference", value.scriptureReference),
        )
    }
}

private fun JSONObject.toAnswer(): AiAnswer {
    val answer = optString("answer").trim()
    if (answer.isBlank()) throw AiServiceException("The explanation service returned an empty response.")
    return AiAnswer(
        text = answer,
        citations = optJSONArray("citations").toObjectList { item ->
            AiCitation(
                title = item.getString("title"),
                url = item.getString("url"),
                startIndex = item.optInt("startIndex", 0),
                endIndex = item.optInt("endIndex", 0),
            )
        }.filter { it.url.startsWith("https://") },
        limitations = optJSONArray("limitations").toStringList(),
    )
}

private fun readLimited(stream: java.io.InputStream?, limit: Int): ByteArray {
    if (stream == null) return byteArrayOf()
    return stream.use { input ->
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8_192)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            total += count
            if (total > limit) throw AiServiceException("The explanation response was too large.")
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
}

private inline fun <T> JSONArray?.toObjectList(transform: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return List(length()) { index -> transform(getJSONObject(index)) }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return List(length()) { index -> getString(index) }
}
