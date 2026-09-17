package org.catholiccompanion.app.data.liturgy

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalendarScopeEntity::class,
        LiturgicalDayEntity::class,
        CelebrationOptionEntity::class,
        ReadingReferenceEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LiturgyDatabase : RoomDatabase() {
    abstract fun liturgyDao(): LiturgyDao

    companion object {
        @Volatile
        private var instance: LiturgyDatabase? = null

        fun create(context: Context): LiturgyDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LiturgyDatabase::class.java,
                "liturgy.db",
            ).build().also { instance = it }
        }
    }
}

