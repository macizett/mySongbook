package com.mayonnaise.mysongbook

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.withContext

class SongViewMusicMode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_view_music_mode)

        var pdfViewSong = findViewById<PDFView>(R.id.pdfViewSong)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)
        var buttonAddToFav: CheckBox = findViewById(R.id.buttonAddToFav)
        var numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.status_bar_color)

        var songNumber = DataManager.chosenSong


        var songbook = when (DataManager.chosenSongbook){
            1 -> "duchowe"
            2 -> "wedrowiec"
            3 -> "bialy"
            else -> {
                Toast.makeText(this, "ERROR READING PDF", Toast.LENGTH_SHORT).show()}
        }

        fun displayPdfFromAsset(fileName: String) {
            pdfViewSong.fromAsset(fileName)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(false)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .load()
        }

        fun updateSong(){
            displayPdfFromAsset("${songbook}${songNumber}.pdf")
            GlobalScope.launch(Dispatchers.IO){
                var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    numberAndTitleTV.text = "${currentSong.number}. ${currentSong.title}"
                }
            }
        }

        try {
            updateSong()
        } catch(e: com.github.barteksc.pdfviewer.exception.FileNotFoundException) {
            Toast.makeText(this, "ERROR OPENING PDF, TRY AGAIN", Toast.LENGTH_LONG).show()
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
            updateSong()
            if(songNumber < DataManager.maxSongNumber && songNumber > 1){
                rightArrowButton.visibility = View.VISIBLE
            }
            else{
                leftArrowButton.visibility = View.INVISIBLE
            }
        }
        rightArrowButton.setOnClickListener{
            songNumber++
            updateSong()
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