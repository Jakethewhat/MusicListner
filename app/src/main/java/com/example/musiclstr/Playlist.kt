package com.example.musiclstr

data class Playlist(val name: String, val songs: List<Song> = emptyList())
