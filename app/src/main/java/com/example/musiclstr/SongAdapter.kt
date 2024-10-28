package com.example.musiclstr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private val songs: List<Song>, // List of songs to display
    private val itemClick: (Song) -> Unit // Lambda function for handling item clicks
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // Inflate the song_item layout to create the view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view) // Return the new ViewHolder
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position] // Get the song at the current position
        holder.bind(song, itemClick) // Pass the song and click listener to bind
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return songs.size // Return the number of songs
    }

    // ViewHolder class to hold the views for each song item
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView) // Title TextView
        private val artistTextView: TextView = itemView.findViewById(R.id.artistTextView) // Artist TextView

        // Bind song data to the views and set the click listener
        fun bind(song: Song, itemClick: (Song) -> Unit) {
            titleTextView.text = song.title // Set song title
            artistTextView.text = song.artist // Set artist name

            // Set the click listener for the item view
            itemView.setOnClickListener {
                itemClick(song) // Trigger the click event with the song
            }
        }
    }
}
