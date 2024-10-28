package com.example.musiclstr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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

class HomeScreen : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songList = mutableListOf<Song>() // List to hold songs
    private var filteredList = mutableListOf<Song>() // List to hold filtered songs
    private var mediaPlayer: MediaPlayer? = null // MediaPlayer instance

    // Control flag
    private var currentSongIndex = -1

    // Declare the pauseResumeButton at the class level
    private lateinit var pauseResumeButton: ImageButton

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize songAdapter
        songAdapter = SongAdapter(filteredList) { song ->
            currentSongIndex = filteredList.indexOf(song)
            playSong(song) // Play song when clicked
            updatePlayPauseButton() // Update play/pause button immediately
        }
        recyclerView.adapter = songAdapter

        // Check for read external storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE)
        } else {
            fetchAllSongs() // Fetch songs if permission is already granted
        }

        // Initialize buttons
        pauseResumeButton = findViewById(R.id.pauseResumeButton) // Initialize the pauseResumeButton here
        val previousButton: ImageButton = findViewById(R.id.previousButton)
        val nextButton: ImageButton = findViewById(R.id.nextButton)
        val playlistButton: ImageButton = findViewById(R.id.playlistButton)

        // Set up button click listeners
        pauseResumeButton.setOnClickListener {
            togglePlayPause(pauseResumeButton)
        }

        previousButton.setOnClickListener {
            Log.d("ButtonClick", "Previous button clicked")
            playPreviousSong()
        }

        nextButton.setOnClickListener {
            Log.d("ButtonClick", "Next button clicked")
            playNextSong()
        }
        playlistButton.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }

        mediaPlayer?.setOnCompletionListener {
            playNextSong() // Play the next song when the current one ends
        }

        // Set up seek bar
        val seekBar: SeekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress * 1000) // Convert seconds to milliseconds
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Initialize SearchView
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText) // Call the filter function on text change
                return true
            }
        })
    }

    private fun fetchAllSongs() {
        Log.d("FetchSongs", "Fetching songs from MediaStore...")

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

        songList.clear() // Clear the existing song list

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val data = it.getString(dataColumn)
                val artist = it.getString(artistColumn) ?: "Unknown"

                val song = Song(id = id, title = name, artist = artist, filePath = data)
                songList.add(song)
                Log.d("FetchSongs", "Found song: ${song.title}, path: ${song.filePath}")
            }
        }

        Log.d("FetchSongs", "Total songs fetched: ${songList.size}")
        filteredList.addAll(songList) // Populate filteredList with all songs initially
        updateRecyclerView() // Update RecyclerView
    }

    private fun updateRecyclerView() {
        songAdapter.notifyDataSetChanged() // Notify adapter of data change
    }

    private fun playSong(song: Song) {
        // If the current song is already playing, release the MediaPlayer and start the new song
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop() // Stop the current song
            mediaPlayer?.release() // Release the current MediaPlayer
        }

        Log.d("PlaySong", "Playing song: ${song.title} by ${song.artist}")

        // Validate file path
        if (song.filePath.isBlank()) {
            Log.e("PlaySong", "Invalid song file path: ${song.filePath}")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.filePath) // Use the file path from the song
                prepareAsync() // Prepare asynchronously
                setOnPreparedListener {
                    start() // Start playing the song once prepared
                    updateUI(song) // Update UI with song info
                    updateSeekBar() // Start updating seek bar
                }
                setOnCompletionListener {
                    Log.d("MediaPlayer", "Song completed.")
                    playNextSong() // Play the next song when the current one ends
                }
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error preparing MediaPlayer: ${e.message}")
        }
    }

    private fun updateUI(song: Song) {
        findViewById<TextView>(R.id.song_title).text = song.title
        findViewById<TextView>(R.id.song_artist).text = song.artist
        updatePlayPauseButton() // Update the play/pause button when a new song is played
    }

    private fun updateSeekBar() {
        val seekBar: SeekBar = findViewById(R.id.seekBar)

        mediaPlayer?.let { player ->
            // Set the maximum value of the seek bar to the duration of the current song
            seekBar.max = player.duration / 1000 // Set max to duration in seconds

            val handler = android.os.Handler(mainLooper)
            val updateSeekBarRunnable = object : Runnable {
                override fun run() {
                    mediaPlayer?.let { mp ->
                        try {
                            if (mp.isPlaying) {
                                val currentPosition = mp.currentPosition / 1000 // Current position in seconds
                                seekBar.progress = currentPosition // Update seek bar on UI thread
                            }else{

                            }
                        } catch (e: IllegalStateException) {
                            Log.e("MediaPlayer", "MediaPlayer is in an invalid state: ${e.message}")
                        }
                    }
                    // Schedule the next update
                    handler.postDelayed(this, 1000) // Update every second
                }
            }

            // Start updating the seek bar
            handler.post(updateSeekBarRunnable)

            // Stop updating the seek bar when the song ends
            player.setOnCompletionListener {
                handler.removeCallbacks(updateSeekBarRunnable) // Stop updates
                seekBar.progress = 0 // Reset seek bar to the beginning
            }
        }
    }

    private fun playNextSong() {
        if (filteredList.isEmpty()) return // Check if song list is empty

        currentSongIndex = (currentSongIndex + 1) % filteredList.size

        playSong(filteredList[currentSongIndex]) // Play the next song
    }

    private fun playPreviousSong() {
        if (filteredList.isEmpty()) return // Check if song list is empty

        currentSongIndex = (currentSongIndex - 1 + filteredList.size) % filteredList.size

        playSong(filteredList[currentSongIndex]) // Play the previous song
    }

    private fun togglePlayPause(button: ImageButton) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause() // Pause the song
            button.setImageResource(R.drawable.play) // Change button icon to play
        } else {
            mediaPlayer?.start() // Resume playing
            button.setImageResource(R.drawable.playing_button) // Change button icon to pause
        }
    }

    private fun updatePlayPauseButton() {
        if (mediaPlayer?.isPlaying == true) {
            pauseResumeButton.setImageResource(R.drawable.playing_button) // Change button icon to pause
        } else {
            pauseResumeButton.setImageResource(R.drawable.play) // Change button icon to play
        }
    }

    private fun filterSongs(query: String?) {
        filteredList.clear() // Clear the current filtered list

        if (query.isNullOrEmpty()) {
            filteredList.addAll(songList) // If query is empty, show all songs
        } else {
            val lowerCaseQuery = query.lowercase()
            for (song in songList) {
                if (song.title.lowercase().contains(lowerCaseQuery) ||
                    song.artist.lowercase().contains(lowerCaseQuery)
                ) {
                    filteredList.add(song) // Add song if it matches the query
                }
            }
        }

        updateRecyclerView() // Update RecyclerView with filtered songs
    }
}
