package com.lifespaces.android.voice

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.SystemClock
import com.lifespaces.android.data.VOICE_NOTE_MAX_DURATION_MS
import java.io.File
import java.util.UUID

data class VoiceNoteDraft(val file: File, val durationMs: Long)

class VoiceNoteRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var startedAt = 0L
    private var output: File? = null

    fun start() {
        val directory = File(context.filesDir, "voice-notes").apply { mkdirs() }
        val file = File(directory, "${UUID.randomUUID()}.m4a")
        MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(48_000)
            setAudioChannels(1)
            setOutputFile(file)
            prepare()
            start()
            recorder = this
            output = file
            startedAt = SystemClock.elapsedRealtime()
        }
    }

    fun elapsedMs(): Long = if (recorder == null) 0 else SystemClock.elapsedRealtime() - startedAt

    fun stop(): VoiceNoteDraft? {
        val active = recorder ?: return null
        val file = output ?: return null
        return try {
            active.stop()
            VoiceNoteDraft(file, elapsedMs())
        } catch (_: RuntimeException) {
            file.delete()
            null
        } finally {
            active.release()
            recorder = null
            output = null
        }
    }

    fun discard() {
        runCatching { recorder?.stop() }
        recorder?.release()
        output?.delete()
        recorder = null
        output = null
    }
}

class VoiceNotePlayer {
    private var player: MediaPlayer? = null

    fun toggle(file: File, onFinished: () -> Unit): Boolean {
        player?.let {
            it.release()
            player = null
            return false
        }
        return runCatching {
            MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    it.release()
                    player = null
                    onFinished()
                }
                prepare()
                start()
                player = this
            }
            true
        }.getOrElse {
            player?.release()
            player = null
            false
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
