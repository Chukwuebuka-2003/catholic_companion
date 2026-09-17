package org.catholiccompanion.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.model.RosaryProgress

private val Context.rosaryDataStore by preferencesDataStore(name = "rosary_progress")

class RosaryProgressRepository(private val context: Context) {
    val progress: Flow<RosaryProgress?> = context.rosaryDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            if (preferences[ACTIVE] != true) return@map null
            val mysterySet = preferences[MYSTERY_SET]
                ?.let { stored -> MysterySet.entries.firstOrNull { it.name == stored } }
                ?: return@map null
            RosaryProgress(
                mysterySet = mysterySet,
                stepIndex = preferences[STEP_INDEX] ?: 0,
            )
        }

    val soundEnabled: Flow<Boolean> = context.rosaryDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[SOUND_ENABLED] ?: true }

    val hapticsEnabled: Flow<Boolean> = context.rosaryDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[HAPTICS_ENABLED] ?: true }

    suspend fun save(progress: RosaryProgress) {
        context.rosaryDataStore.edit { preferences ->
            preferences[ACTIVE] = true
            preferences[MYSTERY_SET] = progress.mysterySet.name
            preferences[STEP_INDEX] = progress.stepIndex
        }
    }

    suspend fun clear() {
        context.rosaryDataStore.edit { preferences ->
            preferences.remove(ACTIVE)
            preferences.remove(MYSTERY_SET)
            preferences.remove(STEP_INDEX)
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.rosaryDataStore.edit { preferences -> preferences[SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.rosaryDataStore.edit { preferences -> preferences[HAPTICS_ENABLED] = enabled }
    }

    private companion object {
        val ACTIVE = booleanPreferencesKey("active")
        val MYSTERY_SET = stringPreferencesKey("mystery_set")
        val STEP_INDEX = intPreferencesKey("step_index")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }
}
