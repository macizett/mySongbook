package com.mayonnaise.mysongbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SongViewMusicMode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_view_music_mode)

        var pdfViewSong = findViewById<PDFView>(R.id.pdfViewSong)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)

        var songNumber = chosenSongStore.chosenSong.toInt()

        pdfViewSong.fromAsset("${chosenSongStore.chosenSongbook}${chosenSongStore.chosenSong}.pdf").load()

        if (songNumber >= chosenSongStore.maxSongNumber){
            rightArrowButton.visibility = View.INVISIBLE
        }
        else{
            rightArrowButton.visibility = View.VISIBLE
        }

        if (songNumber <= 1){
            leftArrowButton.visibility = View.INVISIBLE
        }
        else{
            leftArrowButton.visibility = View.VISIBLE
        }

        leftArrowButton.setOnClickListener{
            songNumber--
            pdfViewSong.fromAsset("${chosenSongStore.chosenSongbook}${songNumber}.pdf").load()
            if(songNumber < chosenSongStore.maxSongNumber && songNumber > 1){
                rightArrowButton.visibility = View.VISIBLE
            }
            else{
                leftArrowButton.visibility = View.INVISIBLE
            }
        }
        rightArrowButton.setOnClickListener{
            songNumber++
            pdfViewSong.fromAsset("${chosenSongStore.chosenSongbook}${songNumber}.pdf").load()
            if(songNumber > 1 && songNumber < chosenSongStore.maxSongNumber){
                leftArrowButton.visibility = View.VISIBLE
            }
            else{
                rightArrowButton.visibility = View.INVISIBLE
            }
        }
    }
}