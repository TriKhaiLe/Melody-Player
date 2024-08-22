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
    private var isPlaying = false

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
                fadeOut()
                if (loopToggleButton.isChecked) {
                    start()  // Replay the music when looping is enabled
                    fadeIn()
                }
            }
        }

        // Play/Pause Button Click Listener
        playPauseButton.setOnClickListener {
            if (isPlaying) {
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
        mediaPlayer.start()
        fadeIn()
        playPauseButton.text = "Pause"
        isPlaying = true
    }

    private fun pauseMusic() {
        fadeOut()
        mediaPlayer.pause()
        playPauseButton.text = "Play"
        isPlaying = false
    }

    private fun fadeIn() {
        val handler = Handler(mainLooper)
        val volumeIncrement = 1.0f / (fadeDuration / 100)

        for (i in 0..100) {
            handler.postDelayed({
                mediaPlayer.setVolume(i * volumeIncrement, i * volumeIncrement)
            }, i * 50L)
        }
    }

    private fun fadeOut() {
        val handler = Handler(mainLooper)
        val volumeDecrement = 1.0f / (fadeDuration / 100)

        for (i in 0..100) {
            handler.postDelayed({
                mediaPlayer.setVolume((100 - i) * volumeDecrement, (100 - i) * volumeDecrement)
            }, i * 50L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
