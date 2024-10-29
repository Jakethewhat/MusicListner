package com.example.musiclstr

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class PlayingSongFragment : Fragment() {
    private lateinit var playPauseButton: ImageView
    private lateinit var previousButton: ImageView
    private lateinit var nextButton: ImageView
    private lateinit var progressBar: SeekBar
    private lateinit var startTextView: TextView
    private lateinit var endTextView: TextView
    private lateinit var songTitleTextView: TextView // Add this line
    private lateinit var songArtistTextView: TextView // Add this line
    private lateinit var handler: Handler
    private lateinit var updateSeekBarRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playing_song, container, false)

        playPauseButton = view.findViewById(R.id.playPauseButton)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)
        progressBar = view.findViewById(R.id.linearProgressBar)
        startTextView = view.findViewById(R.id.startTextView)
        endTextView = view.findViewById(R.id.endTextView)
        songTitleTextView = view.findViewById(R.id.songTitleTextView) // Initialize here
        songArtistTextView = view.findViewById(R.id.songArtistTextView) // Initialize here

        // Set the song title and artist (ensure you have a method to get these values)
        songTitleTextView.text = "Your Song Title" // Replace with actual song title
        songArtistTextView.text = "Your Artist Name" // Replace with actual artist name

        playPauseButton.setOnClickListener {
            (activity as? HomeScreen)?.togglePlayPause()
            updatePlayPauseButton()  // Update UI immediately
        }

        previousButton.setOnClickListener {
            (activity as? HomeScreen)?.playPreviousSong()
        }

        nextButton.setOnClickListener {
            (activity as? HomeScreen)?.playNextSong()
        }

        handler = Handler()
        setupSeekBar()

        return view
    }

    private fun updatePlayPauseButton() {
        if (HomeScreen.sharedMediaPlayer?.isPlaying == true) {
            playPauseButton.setImageResource(R.drawable.playing_button)
        } else {
            playPauseButton.setImageResource(R.drawable.play)
        }
    }

    private fun setupSeekBar() {
        HomeScreen.sharedMediaPlayer?.let { player ->
            progressBar.max = player.duration / 1000
            progressBar.progress = player.currentPosition / 1000

            // Update the endTextView with the total duration
            endTextView.text = formatTime(player.duration)
        }

        updateSeekBarRunnable = Runnable {
            HomeScreen.sharedMediaPlayer?.let { player ->
                progressBar.progress = player.currentPosition / 1000
                startTextView.text = formatTime(player.currentPosition)
            }
            handler.postDelayed(updateSeekBarRunnable, 1000)
        }

        handler.post(updateSeekBarRunnable)

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    HomeScreen.sharedMediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                HomeScreen.sharedMediaPlayer?.seekTo(seekBar.progress * 1000)
                handler.post(updateSeekBarRunnable)
            }
        })
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        updatePlayPauseButton()  // Update play/pause button state when fragment is resumed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable)
    }
}
