package org.catholiccompanion.app.data.liturgy

import android.content.Context
import org.json.JSONObject

private const val GENERAL_ROMAN_2026_ASSET = "calendars/general-roman-2026.json"

class BundledCalendarInstaller(
    private val context: Context,
    private val repository: LiturgyRepository,
) {
    suspend fun installIfNeeded(): Boolean {
        val bundle = loadBundle(GENERAL_ROMAN_2026_ASSET)
        return repository.installIfNeeded(bundle)
    }

    private fun loadBundle(assetPath: String): CalendarBundle {
        val root = context.assets.open(assetPath).bufferedReader().use { reader ->
            JSONObject(reader.readText())
        }
        val scopeJson = root.getJSONObject("scope")
        val scope = CalendarScopeEntity(
            id = scopeJson.getString("id"),
            displayName = scopeJson.getString("displayName"),
            rite = scopeJson.getString("rite"),
            region = scopeJson.getString("region"),
            coverageStart = scopeJson.getString("coverageStart"),
            coverageEnd = scopeJson.getString("coverageEnd"),
            sourceName = scopeJson.getString("sourceName"),
            sourceUrl = scopeJson.getString("sourceUrl"),
            permissionStatus = scopeJson.getString("permissionStatus"),
            contentVersion = scopeJson.getString("contentVersion"),
            reviewedAt = scopeJson.getString("reviewedAt"),
        )
        val days = root.getJSONArray("days").mapObjects { item ->
            LiturgicalDayEntity(
                calendarId = item.getString("calendarId"),
                date = item.getString("date"),
                season = item.getString("season"),
                liturgicalColor = item.getString("liturgicalColor"),
                contentReleaseId = item.getString("contentReleaseId"),
            )
        }
        val celebrations = root.getJSONArray("celebrations").mapObjects { item ->
            CelebrationOptionEntity(
                id = item.getString("id"),
                calendarId = item.getString("calendarId"),
                date = item.getString("date"),
                title = item.getString("title"),
                rank = item.getString("rank"),
                isPrimary = item.getBoolean("isPrimary"),
            )
        }
        val readings = root.getJSONArray("readings").mapObjects { item ->
            ReadingReferenceEntity(
                id = item.getString("id"),
                celebrationId = item.getString("celebrationId"),
                orderIndex = item.getInt("orderIndex"),
                label = item.getString("label"),
                citation = item.getString("citation"),
                permittedText = if (item.isNull("permittedText")) {
                    null
                } else {
                    item.getString("permittedText")
                },
                sourceDocumentTitle = item.getString("sourceDocumentTitle"),
                sourceUrl = item.getString("sourceUrl"),
            )
        }
        return CalendarBundle(scope, days, celebrations, readings)
    }
}

private inline fun <T> org.json.JSONArray.mapObjects(
    transform: (JSONObject) -> T,
): List<T> = List(length()) { index -> transform(getJSONObject(index)) }

