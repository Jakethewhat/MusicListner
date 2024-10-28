package com.example.musiclstr

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistActivity : AppCompatActivity() {

    private lateinit var createPlaylistButton: Button
    private lateinit var playlistNameEditText: EditText
    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var songsAdapter: SongAdapter
    private lateinit var playlistAdapter: PlaylistAdapter

    private val playlists = mutableListOf<Playlist>()
    private val songs = mutableListOf<Song>() // Populate this with your available songs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        createPlaylistButton = findViewById(R.id.createPlaylistButton)
        playlistNameEditText = findViewById(R.id.playlistNameEditText)
        songsRecyclerView = findViewById(R.id.songsRecyclerView)
        playlistsRecyclerView = findViewById(R.id.playlistsRecyclerView)

        // Initialize RecyclerViews
        songsAdapter = SongAdapter(songs) { song -> playSong(song) }
        songsRecyclerView.layoutManager = LinearLayoutManager(this)
        songsRecyclerView.adapter = songsAdapter

        playlistsRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistsRecyclerView.adapter = PlaylistAdapter(playlists)

        createPlaylistButton.setOnClickListener {
            val playlistName = playlistNameEditText.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                val newPlaylist = Playlist(playlistName)
                playlists.add(newPlaylist)

                // Update playlists RecyclerView
                playlistsRecyclerView.adapter?.notifyDataSetChanged()

                Toast.makeText(this, "Playlist '$playlistName' created!", Toast.LENGTH_SHORT).show()
                playlistNameEditText.text.clear()
            } else {
                Toast.makeText(this, "Please enter a playlist name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSong(song: Song) {
        Toast.makeText(this, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
    }
}
