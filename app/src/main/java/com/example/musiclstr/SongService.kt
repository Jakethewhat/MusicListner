package com.example.musiclstr

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SongService {
    @GET("song")
    fun getAllSongs(): Call<List<Song>>

    @POST("song")
    fun addSong(@Body song: Song): Call<Song>

    @GET("song/{id}")
    fun getSongById(@Path("id") id: Int): Call<Song>
}
