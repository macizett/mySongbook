package com.mayonnaise.mysongbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SongViewMusicMode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_view_music_mode)

        var pdfViewSong = findViewById<PDFView>(R.id.pdfViewSong)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)
        var buttonAddToFav: CheckBox = findViewById(R.id.buttonAddToFav)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        var songNumber = DataManager.chosenSong
        var songbook = when (DataManager.chosenSongbook){
            1 -> "duchowe"
            2 -> "wedrowiec"
            3 -> "bialy"
            else -> {
                Toast.makeText(this, "ERROR READING PDF", Toast.LENGTH_SHORT).show()}
        }

        GlobalScope.launch(Dispatchers.IO) {
            pdfViewSong.fromAsset("${songbook}${DataManager.chosenSong}.pdf").load()
        }

        if (songNumber >= DataManager.maxSongNumber){
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
            pdfViewSong.fromAsset("${songbook}${songNumber}.pdf").load()
            if(songNumber < DataManager.maxSongNumber && songNumber > 1){
                rightArrowButton.visibility = View.VISIBLE
            }
            else{
                leftArrowButton.visibility = View.INVISIBLE
            }
        }
        rightArrowButton.setOnClickListener{
            songNumber++
            pdfViewSong.fromAsset("${songbook}${songNumber}.pdf").load()
            if(songNumber > 1 && songNumber < DataManager.maxSongNumber){
                leftArrowButton.visibility = View.VISIBLE
            }
            else{
                rightArrowButton.visibility = View.INVISIBLE
            }
        }

        buttonAddToFav.setOnCheckedChangeListener{buttonView, isChecked ->
            if (isChecked){
                GlobalScope.launch(Dispatchers.IO) {
                    var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = true
                    songDao.updateFavoriteSongs(currentSong)
                }
            }else{
                GlobalScope.launch(Dispatchers.IO) {
                    var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = false
                    songDao.updateFavoriteSongs(currentSong)
                }
            }
        }

    }

}