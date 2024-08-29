package com.example.melodyplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: Button
    private lateinit var loopToggleButton: ToggleButton
    private val fadeDuration = 5000L // 5 seconds
    private var isLMusicPlaying = false
    private val songDuration = 15000L // 15 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.playPauseButton)
        loopToggleButton = findViewById(R.id.loopToggleButton)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            setDataSource("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1k7wMvWeR2ZMnbAtRXCxsfd4WRo_B_cLE")
            prepareAsync()
            setOnPreparedListener {
                playPauseButton.isEnabled = true
            }
            setOnCompletionListener {
                if (loopToggleButton.isChecked) {
                    mediaPlayer.seekTo(0)
                    playMusic()  // Automatically play again with fade-in
                } else {
                    isLMusicPlaying = false
                    playPauseButton.text = "Play"
                }
            }
        }

        // Play/Pause Button Click Listener
        playPauseButton.setOnClickListener {
            if (isLMusicPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        // Loop Toggle Button Listener
        loopToggleButton.setOnCheckedChangeListener { _, isChecked ->
            mediaPlayer.isLooping = false // Custom loop handling
        }
    }

    private fun playMusic() {
        fadeIn {
            scheduleFadeOut()
            playPauseButton.text = "Pause"
            isLMusicPlaying = true
        }
    }

    private fun pauseMusic() {
        fadeOut {
            mediaPlayer.pause()
            playPauseButton.text = "Play"
            isLMusicPlaying = false
        }
    }

    private fun fadeIn(onFadeComplete: () -> Unit) {
        val handler = Handler(mainLooper)
        val volumeIncrement = 1.0f / 100f
        var currentVolume = 0f

        for (i in 0..100) {
            handler.postDelayed({
                if (i == 0) {
                    mediaPlayer.setVolume(0f, 0f)
                    mediaPlayer.start()
                }
                currentVolume = i * volumeIncrement
                mediaPlayer.setVolume(currentVolume, currentVolume)
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
                mediaPlayer.setVolume(currentVolume, currentVolume)
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
        }, songDuration - 2*fadeDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
