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
    private var isMusicPlaying = false

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
                fadeOut {
                    if (loopToggleButton.isChecked) {
                        mediaPlayer.seekTo(0) // Restart the track
                        playMusic()  // Automatically play again with fade-in
                    } else {
                        isMusicPlaying = false
                        playPauseButton.text = "Play"
                    }
                }
            }
        }

        // Play/Pause Button Click Listener
        playPauseButton.setOnClickListener {
            if (isMusicPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        // Loop Toggle Button Listener
        loopToggleButton.setOnCheckedChangeListener { _, isChecked ->
            mediaPlayer.isLooping = isChecked
        }
    }

    private fun playMusic() {
        fadeIn {
            mediaPlayer.start()
            playPauseButton.text = "Pause"
            isMusicPlaying = true
        }
    }

    private fun pauseMusic() {
        fadeOut {
            mediaPlayer.pause()
            playPauseButton.text = "Play"
            isMusicPlaying = false
        }
    }

    private fun fadeIn(onFadeComplete: () -> Unit) {
        val handler = Handler(mainLooper)
        val volumeIncrement = 1.0f / (fadeDuration / 100)
        var currentVolume = 0f

        for (i in 0..100) {
            handler.postDelayed({
                currentVolume = i * volumeIncrement
                mediaPlayer.setVolume(currentVolume, currentVolume)
                if (i == 100) {
                    onFadeComplete()  // Trigger when fade-in is complete
                }
            }, i * (fadeDuration / 100))
        }
    }

    private fun fadeOut(onFadeComplete: () -> Unit) {
        val handler = Handler(mainLooper)
        val volumeDecrement = 1.0f / (fadeDuration / 100)
        var currentVolume = 1.0f

        for (i in 0..100) {
            handler.postDelayed({
                currentVolume = (100 - i) * volumeDecrement
                mediaPlayer.setVolume(currentVolume, currentVolume)
                if (i == 100) {
                    onFadeComplete()  // Trigger when fade-out is complete
                }
            }, i * (fadeDuration / 100))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
