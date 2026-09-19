package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

object StarkSoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playHudBlip() {
        playTone(1800.0, 40, 0.3f)
    }

    fun playArmorLock() {
        scope.launch {
            playToneSync(784.0, 60, 0.35f) // G5
            delay(30)
            playToneSync(1174.66, 90, 0.45f) // D6
        }
    }

    fun playRepulsorCharge() {
        scope.launch {
            val sampleRate = 44100
            val durationMs = 380
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                // Frequency sweeps from 350Hz up to 2600Hz
                val freq = 350.0 + (progress * progress) * 2250.0
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val envelope = sin(progress * Math.PI)
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.45f * envelope).toInt().toShort()
            }
            playPcm(buffer, sampleRate)
        }
    }

    fun playUnibeamBlast() {
        scope.launch {
            // First charge up, then big resonant pulse
            val sampleRate = 44100
            val durationMs = 450
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = if (progress < 0.3) {
                    400.0 + progress * 3000.0
                } else {
                    180.0 + (1.0 - progress) * 120.0
                }
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val envelope = if (progress < 0.1) progress / 0.1 else (1.0 - progress)
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.5f * envelope).toInt().toShort()
            }
            playPcm(buffer, sampleRate)
        }
    }

    fun playReactorHum() {
        playTone(90.0, 240, 0.45f)
    }

    fun playAlertKlaxon() {
        scope.launch {
            playToneSync(880.0, 90, 0.45f)
            playToneSync(660.0, 110, 0.45f)
        }
    }

    fun playProtocolSuccess() {
        scope.launch {
            playToneSync(523.25, 70, 0.3f) // C5
            delay(20)
            playToneSync(659.25, 70, 0.35f) // E5
            delay(20)
            playToneSync(1046.50, 120, 0.4f) // C6
        }
    }

    private fun playTone(freq: Double, durationMs: Int, volume: Float) {
        scope.launch {
            playToneSync(freq, durationMs, volume)
        }
    }

    private fun playToneSync(freq: Double, durationMs: Int, volume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val angle = 2.0 * Math.PI * i / (sampleRate / freq)
            val envelope = if (progress < 0.15) progress / 0.15 else if (progress > 0.8) (1.0 - progress) / 0.2 else 1.0
            buffer[i] = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt().toShort()
        }
        playPcm(buffer, sampleRate)
    }

    private fun playPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            audioTrack.setNotificationMarkerPosition(buffer.size)
            audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(track: AudioTrack?) {}
                override fun onMarkerReached(track: AudioTrack?) {
                    try {
                        track?.stop()
                        track?.release()
                    } catch (_: Exception) {}
                }
            })
        } catch (_: Exception) {}
    }
}
