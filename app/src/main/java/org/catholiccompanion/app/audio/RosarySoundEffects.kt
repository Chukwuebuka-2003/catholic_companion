package org.catholiccompanion.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Short, low-latency Rosary sounds backed by SoundPool for broad OEM compatibility. */
class RosarySoundEffects(context: Context) : AutoCloseable {
    private val lock = Any()
    private val readySounds = mutableSetOf<Int>()
    private val pendingSounds = mutableSetOf<Int>()
    private var isClosed = false
    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    } catch (_: Throwable) {
        null
    }
    private var beadSoundId = 0
    private var completionSoundId = 0

    init {
        val pool = soundPool
        if (pool != null) {
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                val shouldPlay = synchronized(lock) {
                    if (isClosed || (status != 0)) return@setOnLoadCompleteListener
                    readySounds += sampleId
                    pendingSounds.remove(sampleId)
                }
                if (shouldPlay) playLoaded(sampleId)
            }

            beadSoundId = load(
                file = File(context.cacheDir, "rosary_wood_bead_v2.wav"),
                samples = woodBeadClick(),
            )
            completionSoundId = load(
                file = File(context.cacheDir, "rosary_completion_v2.wav"),
                samples = completionChime(),
            )
        }
    }

    fun playBead() = playOrQueue(beadSoundId)

    fun playCompletion() = playOrQueue(completionSoundId)

    override fun close() {
        synchronized(lock) {
            if (isClosed) return
            isClosed = true
            readySounds.clear()
            pendingSounds.clear()
        }
        soundPool?.release()
    }

    private fun load(file: File, samples: ShortArray): Int = runCatching {
        val pool = soundPool ?: return 0
        writeWaveFile(file, samples)
        pool.load(file.absolutePath, 1)
    }.getOrDefault(0)

    private fun playOrQueue(soundId: Int) {
        if ((soundId == 0) || (soundPool == null)) return
        val playNow = synchronized(lock) {
            if (isClosed) return
            if (soundId in readySounds) true else {
                pendingSounds += soundId
                false
            }
        }
        if (playNow) playLoaded(soundId)
    }

    private fun playLoaded(soundId: Int) {
        val pool = soundPool ?: return
        val volume = if (soundId == completionSoundId) 0.72f else 0.82f
        pool.play(soundId, volume, volume, 1, 0, 1f)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val WAVE_HEADER_BYTES = 44

        fun writeWaveFile(file: File, samples: ShortArray) {
            val dataSize = samples.size * Short.SIZE_BYTES
            val buffer = ByteBuffer.allocate(WAVE_HEADER_BYTES + dataSize)
                .order(ByteOrder.LITTLE_ENDIAN)
            buffer.put("RIFF".encodeToByteArray())
            buffer.putInt(36 + dataSize)
            buffer.put("WAVE".encodeToByteArray())
            buffer.put("fmt ".encodeToByteArray())
            buffer.putInt(16)
            buffer.putShort(1) // PCM
            buffer.putShort(1) // Mono
            buffer.putInt(SAMPLE_RATE)
            buffer.putInt(SAMPLE_RATE * Short.SIZE_BYTES)
            buffer.putShort(Short.SIZE_BYTES.toShort())
            buffer.putShort(16)
            buffer.put("data".encodeToByteArray())
            buffer.putInt(dataSize)
            samples.forEach(buffer::putShort)
            file.outputStream().use { output -> output.write(buffer.array()) }
        }

        fun woodBeadClick(): ShortArray {
            val random = Random(47)
            return ShortArray((SAMPLE_RATE * 0.075).toInt()) { index ->
                val time = index.toDouble() / SAMPLE_RATE
                val impact = exp(-time * 62.0)
                val knock = sin(2.0 * PI * 760.0 * time) * 0.66 +
                    sin(2.0 * PI * 1_420.0 * time) * 0.24
                val grain = (random.nextDouble() * 2.0 - 1.0) * 0.25
                ((knock + grain) * impact * Short.MAX_VALUE * 0.58)
                    .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        fun completionChime(): ShortArray = ShortArray((SAMPLE_RATE * 0.55).toInt()) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val envelope = exp(-time * 5.2)
            val tone = sin(2.0 * PI * 784.0 * time) * 0.55 +
                sin(2.0 * PI * 1_176.0 * time) * 0.28 +
                sin(2.0 * PI * 1_568.0 * time) * 0.12
            (tone * envelope * Short.MAX_VALUE * 0.38)
                .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
}
