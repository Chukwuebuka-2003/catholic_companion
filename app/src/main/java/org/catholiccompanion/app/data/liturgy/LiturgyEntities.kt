package org.catholiccompanion.app.data.liturgy

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "calendar_scopes", primaryKeys = ["id"])
data class CalendarScopeEntity(
    val id: String,
    val displayName: String,
    val rite: String,
    val region: String,
    val coverageStart: String,
    val coverageEnd: String,
    val sourceName: String,
    val sourceUrl: String,
    val permissionStatus: String,
    val contentVersion: String,
    val reviewedAt: String,
)

@Entity(
    tableName = "liturgical_days",
    primaryKeys = ["calendarId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = CalendarScopeEntity::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("calendarId")],
)
data class LiturgicalDayEntity(
    val calendarId: String,
    val date: String,
    val season: String,
    val liturgicalColor: String,
    val contentReleaseId: String,
)

@Entity(
    tableName = "celebration_options",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = LiturgicalDayEntity::class,
            parentColumns = ["calendarId", "date"],
            childColumns = ["calendarId", "date"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["calendarId", "date"])],
)
data class CelebrationOptionEntity(
    val id: String,
    val calendarId: String,
    val date: String,
    val title: String,
    val rank: String,
    val isPrimary: Boolean,
)

@Entity(
    tableName = "reading_references",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = CelebrationOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["celebrationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("celebrationId")],
)
data class ReadingReferenceEntity(
    val id: String,
    val celebrationId: String,
    val orderIndex: Int,
    val label: String,
    val citation: String,
    val permittedText: String?,
    val sourceDocumentTitle: String,
    val sourceUrl: String,
)

