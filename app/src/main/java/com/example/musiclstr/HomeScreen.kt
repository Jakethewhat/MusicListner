package com.example.musiclstr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HomeScreen : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songList = mutableListOf<Song>()
    private var filteredList = mutableListOf<Song>()
    private var currentSongIndex = -1
    private lateinit var handler: Handler
    private lateinit var updateSeekBarRunnable: Runnable

    private lateinit var pauseResumeButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var positivePlaybackTimer: TextView
    private lateinit var negativePlaybackTimer: TextView

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
        var sharedMediaPlayer: MediaPlayer? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUIComponents()
        checkStoragePermission()
        setupSeekBarUpdate()
    }

    private fun initializeUIComponents() {
        // Initialize RecyclerView and SongAdapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(filteredList) { song ->
            currentSongIndex = filteredList.indexOf(song)
            playSong(song)
            updatePlayPauseButton()
        }
        recyclerView.adapter = songAdapter

        // Initialize and set up control buttons
        pauseResumeButton = findViewById(R.id.pauseResumeButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        seekBar = findViewById(R.id.seekBar)

        // Initialize playback timers
        positivePlaybackTimer = findViewById(R.id.positive_playback_timer)
        negativePlaybackTimer = findViewById(R.id.negative_playback_timer)

        // Set click listeners
        pauseResumeButton.setOnClickListener { togglePlayPause() }
        previousButton.setOnClickListener { playPreviousSong() }
        nextButton.setOnClickListener { playNextSong() }

        // Initialize Playlist button
        findViewById<ImageButton>(R.id.playlistButton).setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        // Initialize navigation to PlayingSongFragment
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlayingSongFragment())
                .addToBackStack(null)
                .commit()
        }

        // Initialize SearchView with a query text listener
        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText)
                return true
            }
        })
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
        } else {
            fetchAllSongs()
        }
    }

    private fun fetchAllSongs() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )

        songList.clear()
        cursor?.use {
            while (it.moveToNext()) {
                val song = Song(
                    id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)),
                    artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown",
                    filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                )
                songList.add(song)
            }
        }
        filteredList.addAll(songList)
        songAdapter.notifyDataSetChanged()
    }

    private fun playSong(song: Song) {
        sharedMediaPlayer?.stop()
        sharedMediaPlayer?.release()

        sharedMediaPlayer = MediaPlayer().apply {
            setDataSource(song.filePath)
            prepareAsync()
            setOnPreparedListener {
                start()
                updateUI(song)
                handler.post(updateSeekBarRunnable)
            }
            setOnCompletionListener { playNextSong() }
        }
    }

    private fun updateUI(song: Song) {
        findViewById<TextView>(R.id.song_title).text = song.title
        findViewById<TextView>(R.id.song_artist).text = song.artist
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        if (sharedMediaPlayer?.isPlaying == true) {
            pauseResumeButton.setImageResource(R.drawable.playing_button)
        } else {
            pauseResumeButton.setImageResource(R.drawable.play)
        }
    }

    fun togglePlayPause() {
        sharedMediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                pauseResumeButton.setImageResource(R.drawable.play)
            } else {
                player.start()
                pauseResumeButton.setImageResource(R.drawable.playing_button)
            }
        }
    }

    fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            playSong(filteredList[currentSongIndex])
        } else {
            Toast.makeText(this, "No previous song", Toast.LENGTH_SHORT).show()
        }
    }

    fun playNextSong() {
        if (currentSongIndex < filteredList.size - 1) {
            currentSongIndex++
            playSong(filteredList[currentSongIndex])
        } else {
            Toast.makeText(this, "No next song", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterSongs(query: String?) {
        filteredList.clear()
        filteredList.addAll(
            if (query.isNullOrEmpty()) songList
            else songList.filter { it.title.contains(query, ignoreCase = true) }
        )
        songAdapter.notifyDataSetChanged()
    }

    private fun setupSeekBarUpdate() {
        handler = Handler(mainLooper)

        updateSeekBarRunnable = Runnable {
            sharedMediaPlayer?.let { player ->
                seekBar.max = player.duration / 1000
                seekBar.progress = player.currentPosition / 1000

                // Update playback timers
                val elapsedTime = player.currentPosition / 1000 // seconds
                val remainingTime = player.duration / 1000 - elapsedTime // seconds

                positivePlaybackTimer.text = String.format(Locale.getDefault(), "%02d:%02d", elapsedTime / 60, elapsedTime % 60)
                negativePlaybackTimer.text = String.format(Locale.getDefault(), "-%02d:%02d", remainingTime / 60, remainingTime % 60)
            }
            handler.postDelayed(updateSeekBarRunnable, 1000)
        }

        // Start the periodic update
        handler.post(updateSeekBarRunnable)

        // SeekBar change listener to handle manual seeking
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Update position based on user input while dragging
                    sharedMediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                handler.removeCallbacks(updateSeekBarRunnable) // Pause updates while dragging
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sharedMediaPlayer?.seekTo(seekBar.progress * 1000) // Seek to new position
                handler.post(updateSeekBarRunnable) // Resume updates
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBarRunnable)
        sharedMediaPlayer?.release()
    }
}
