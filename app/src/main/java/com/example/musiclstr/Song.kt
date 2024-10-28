package com.example.musiclstr

// Ensure this is defined only once

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val filePath: String // This should match the path you're using to play the song

)


