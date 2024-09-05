package com.example.melodyplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playPauseButton: Button
    private lateinit var loopToggleButton: ToggleButton
    private val fadeDuration = 5000L // 5 seconds
    private val songDuration = 15000L // 15 seconds
    private var isMusicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.playPauseButton)
        loopToggleButton = findViewById(R.id.loopToggleButton)

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true
            )
            setMediaItem(MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1k7wMvWeR2ZMnbAtRXCxsfd4WRo_B_cLE"))
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            // The player is ready to play, enable the button
                            playPauseButton.isEnabled = true
                        }
                        Player.STATE_ENDED -> {
                            if (loopToggleButton.isChecked) {
                                seekTo(0)
                                playMusic() // Automatically play again with fade-in
                            } else {
                                isMusicPlaying = false
                                playPauseButton.text = "Play"
                                playPauseButton.isEnabled = true
                            }
                        }
                    }

                }
            })
        }

        // Play/Pause Button Click Listener
        playPauseButton.setOnClickListener {
            playPauseButton.isEnabled = false
            playMusic()
        }

        // Loop Toggle Button Listener
        loopToggleButton.setOnCheckedChangeListener { _, _ ->
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF // Custom loop handling
        }
    }

    private fun playMusic() {
        fadeIn {
            scheduleFadeOut()
            isMusicPlaying = true
        }
    }

    private fun fadeIn(onFadeComplete: () -> Unit) {
        val handler = Handler(mainLooper)
        val volumeIncrement = 1.0f / 100f
        var currentVolume = 0f

        for (i in 0..100) {
            handler.postDelayed({
                if (i == 0) {
                    exoPlayer.volume = 0f
                    exoPlayer.play()
                }
                currentVolume = i * volumeIncrement
                exoPlayer.volume = currentVolume
                if (i == 100) {
                    onFadeComplete()
                }
            }, i * (fadeDuration / 100))
        }
    }

    private fun fadeOut(onFadeComplete: () -> Unit) {
        val handler = Handler(mainLooper)
        val volumeDecrement = 1.0f / 100f
        var currentVolume = 1.0f

        for (i in 0..100) {
            handler.postDelayed({
                currentVolume = (100 - i) * volumeDecrement
                exoPlayer.volume = currentVolume
                if (i == 100) {
                    onFadeComplete()
                }
            }, i * (fadeDuration / 100))
        }
    }

    private fun scheduleFadeOut() {
        val handler = Handler(mainLooper)
        handler.postDelayed({
            fadeOut {}
        }, songDuration - 2 * fadeDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
    }
}
