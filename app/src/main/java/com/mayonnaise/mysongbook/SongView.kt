package com.mayonnaise.mysongbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SongView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songview)

        val songTV: TextView = findViewById(R.id.songTV)
        val numberTV: TextView = findViewById(R.id.numberTV)
        val titleTV: TextView = findViewById(R.id.titleTV)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)

        val songNumbero = chosenSongStore.chosenSong
        var songNumber = songNumbero.toInt()

        var songbook = songbook_duchowe.duchowe
        if(chosenSongStore.chosenSongbook == "duchowe"){
            songbook = songbook_duchowe.duchowe
        }
        if(chosenSongStore.chosenSongbook == "wedrowiec"){
            songbook = songbook_wedrowiec.wedrowiec
            titleTV.setVisibility(View.VISIBLE)
        }
        if(chosenSongStore.chosenSongbook == "bialy"){
            songbook = songbook_bialy.bialy
        }



        numberTV.setText(songNumber.toString()+".")
        songNumber--
        songTV.setText(songbook[songNumber])
        titleTV.setText(songbook_wedrowiec.wedrowiecTitles[songNumber])

        if (songNumber >= chosenSongStore.maxSongNumber-1){
            rightArrowButton.visibility = View.INVISIBLE
        }
        else{
            rightArrowButton.visibility = View.VISIBLE
        }

        if (songNumber <= 0){
            leftArrowButton.visibility = View.INVISIBLE
        }
        else{
            leftArrowButton.visibility = View.VISIBLE
        }

        rightArrowButton.setOnClickListener{
            songNumber++
            songTV.setText(songbook[songNumber])
            numberTV.setText((songNumber+1).toString()+".")
            titleTV.setText(songbook_wedrowiec.wedrowiecTitles[songNumber])
            if(songNumber > 0 && songNumber < chosenSongStore.maxSongNumber-1){
                leftArrowButton.visibility = View.VISIBLE
            }
            else{
                rightArrowButton.visibility = View.INVISIBLE
            }
        }

        leftArrowButton.setOnClickListener{
            songNumber--
            songTV.setText(songbook[songNumber])
            numberTV.setText((songNumber+1).toString()+".")
            titleTV.setText(songbook_wedrowiec.wedrowiecTitles[songNumber])
            if(songNumber < chosenSongStore.maxSongNumber-1 && songNumber > 0){
                rightArrowButton.visibility = View.VISIBLE
            }
            else{
                leftArrowButton.visibility = View.INVISIBLE
            }
        }
    }
}