package com.example.melodyplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlin.random.Random

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playPauseButton: Button
    private lateinit var loopToggleButton: ToggleButton
    private lateinit var addSongButton: Button
    private val fadeDuration = 5000L // 5 seconds
    private val songDuration = 15000L // 15 seconds
    private var isMusicPlaying = false

    // Add multiple media items to create a playlist
    val song1 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1k7wMvWeR2ZMnbAtRXCxsfd4WRo_B_cLE")
    val song2 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/192ksOOkTWa4cceE2mlqgSkELzsFRyjjM")
    val song3 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1WRPqqN9_oPOHFVl9Lp1uH-YpgWSO715s")
    val song4 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1Qa5ikPc-0oJgffHP-pqXtas6nKRi10oB")
    val song5 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1-LvKDOijwoLaVKIdj2H3qQQx1-pamf23")
    val song6 = MediaItem.fromUri("https://un-silent-backend-mobile.azurewebsites.net/api/v1/musics/file/1AEtQy1KMmRlMPWPd6OMMeZCNmiekEbU4")
    val songList = listOf(song1, song2, song3, song4, song5, song6)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.playPauseButton)
        addSongButton = findViewById(R.id.addSongButton)
        loopToggleButton = findViewById(R.id.loopToggleButton)

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true
            )

            // Add songs to the ExoPlayer's media queue
            addMediaItem(song6)

            // Prepare the player to start playing
            prepare()
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            // The player is ready to play, enable the button
                            playMusic()
//                            addNewSongToPlaylist("")
                        }
                        Player.STATE_ENDED -> {
                            if (exoPlayer.hasNextMediaItem()) {
                                // If there's another song in the playlist, move to the next and apply fade-in
                                exoPlayer.seekToNextMediaItem()
                                Toast.makeText(this@MainActivity, "Next Item", Toast.LENGTH_SHORT).show()
                                playMusic()
                            } else {
                                isMusicPlaying = false
                                playPauseButton.text = "Play"
                                playPauseButton.isEnabled = true

                                Toast.makeText(this@MainActivity, "Playlist ended", Toast.LENGTH_SHORT).show()
                            }
                        }
                            Player.STATE_IDLE -> {
                                // The player is in the idle state, disable the button
                                playPauseButton.isEnabled = false
                                playPauseButton.text = "Play"
                                Toast.makeText(this@MainActivity, "STATE_IDLE", Toast.LENGTH_SHORT).show()
//                                prepare()
                            }
                            Player.STATE_BUFFERING -> {
                                // The player is buffering, disable the button
                                playPauseButton.isEnabled = false
                                playPauseButton.text = "Play"
                                Toast.makeText(this@MainActivity, "STATE_BUFFERING", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@MainActivity, "REPEAT_MODE_OFF", Toast.LENGTH_SHORT).show()
        }

        // Add song button click listener
        addSongButton.setOnClickListener {
            // Fetch and append a new song to the ExoPlayer playlist
            addNewSongToPlaylist("")
        }

    }


    // Method to add a new song to the ExoPlayer playlist
    private fun addNewSongToPlaylist(songUrl: String) {
        // Create a new MediaItem from the song URL
        val newSong = pickRandomSong()
        exoPlayer.addMediaItem(newSong)  // Append the new song to the playlist
        exoPlayer.prepare()  // Prepare ExoPlayer after adding the new song
    }

    fun pickRandomSong(): MediaItem {
        val randomIndex = Random.nextInt(songList.size)
        return songList[randomIndex]
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
