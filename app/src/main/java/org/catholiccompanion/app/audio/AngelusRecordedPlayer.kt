package org.catholiccompanion.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.catholiccompanion.app.R
import org.catholiccompanion.app.model.AngelusLanguage

data class AngelusNarrationState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPartIndex: Int? = null,
    val activeLanguage: AngelusLanguage? = null,
    val voiceNotice: String? = null,
    val error: String? = null,
)

/** Plays the bundled Angelus recordings without requiring connectivity. */
class AngelusRecordedPlayer(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(
        AngelusNarrationState(
            isReady = true,
            voiceNotice = "Recorded with Qwen3-TTS 1.7B.",
        ),
    )
    val state: StateFlow<AngelusNarrationState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var playbackToken = 0

    fun play(language: AngelusLanguage, startIndex: Int = 0) {
        val resources = recordings(language)
        val firstIndex = startIndex.coerceIn(resources.indices)
        releasePlayer()
        playbackToken += 1
        _state.update {
            it.copy(
                isPlaying = true,
                isPaused = false,
                currentPartIndex = firstIndex,
                activeLanguage = language,
                error = null,
            )
        }
        playPart(resources, language, firstIndex, playbackToken)
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (!_state.value.isPlaying || !player.isPlaying) return
        player.pause()
        _state.update { it.copy(isPlaying = false, isPaused = true) }
    }

    fun resume() {
        val player = mediaPlayer ?: return
        if (!_state.value.isPaused) return
        player.start()
        _state.update { it.copy(isPlaying = true, isPaused = false, error = null) }
    }

    fun stop() {
        playbackToken += 1
        releasePlayer()
        _state.update {
            it.copy(
                isPlaying = false,
                isPaused = false,
                currentPartIndex = null,
                activeLanguage = null,
                error = null,
            )
        }
    }

    override fun close() {
        stop()
    }

    private fun playPart(
        resources: List<Int>,
        language: AngelusLanguage,
        index: Int,
        token: Int,
    ) {
        if (token != playbackToken) return
        val player = runCatching { createPlayer(resources[index]) }
            .getOrElse { error ->
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isPaused = false,
                        error = "The bundled prayer audio could not be opened: ${error.message.orEmpty()}",
                    )
                }
                return
            }
        mediaPlayer = player
        _state.update {
            it.copy(
                isPlaying = true,
                isPaused = false,
                currentPartIndex = index,
                activeLanguage = language,
                error = null,
            )
        }
        player.setOnCompletionListener { completed ->
            completed.release()
            if (mediaPlayer === completed) mediaPlayer = null
            if (token != playbackToken) return@setOnCompletionListener
            val next = index + 1
            if (next < resources.size) {
                playPart(resources, language, next, token)
            } else {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isPaused = false,
                        currentPartIndex = null,
                    )
                }
            }
        }
        player.setOnErrorListener { failed, _, _ ->
            failed.release()
            if (mediaPlayer === failed) mediaPlayer = null
            if (token == playbackToken) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isPaused = false,
                        error = "The bundled prayer audio stopped unexpectedly.",
                    )
                }
            }
            true
        }
        player.start()
    }

    private fun createPlayer(resourceId: Int): MediaPlayer {
        val descriptor = appContext.resources.openRawResourceFd(resourceId)
        return descriptor.use { audio ->
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                setDataSource(audio.fileDescriptor, audio.startOffset, audio.length)
                prepare()
            }
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    private fun recordings(language: AngelusLanguage): List<Int> = when (language) {
        AngelusLanguage.ENGLISH -> ENGLISH_RECORDINGS
        AngelusLanguage.LATIN -> LATIN_RECORDINGS
    }

    private companion object {
        val ENGLISH_RECORDINGS = listOf(
            R.raw.angelus_en_declaration,
            R.raw.angelus_en_conceived,
            R.raw.angelus_en_hail_mary,
            R.raw.angelus_en_handmaid,
            R.raw.angelus_en_according,
            R.raw.angelus_en_hail_mary,
            R.raw.angelus_en_word_flesh,
            R.raw.angelus_en_dwelt,
            R.raw.angelus_en_hail_mary,
            R.raw.angelus_en_pray_for_us,
            R.raw.angelus_en_promises,
            R.raw.angelus_en_closing,
        )

        val LATIN_RECORDINGS = listOf(
            R.raw.angelus_la_declaration,
            R.raw.angelus_la_conceived,
            R.raw.angelus_la_hail_mary,
            R.raw.angelus_la_handmaid,
            R.raw.angelus_la_according,
            R.raw.angelus_la_hail_mary,
            R.raw.angelus_la_word_flesh,
            R.raw.angelus_la_dwelt,
            R.raw.angelus_la_hail_mary,
            R.raw.angelus_la_pray_for_us,
            R.raw.angelus_la_promises,
            R.raw.angelus_la_closing,
        )
    }
}
