package com.mayonnaise.mysongbook4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.material.slider.Slider
import kotlinx.coroutines.withContext

class SongViewMusicMode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_view_music_mode)

        var pdfViewSong = findViewById<PDFView>(R.id.pdfViewSong)
        var buttonAddToFav: CheckBox = findViewById(R.id.buttonAddToFav)
        var numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)


        val songDao = SongbookDatabase.getInstance(this).songDao()

        numberAndTitleTV.textSize = DataManager.textSize

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.status_bar_color)

        var songNumber = DataManager.chosenSong

        fun animateButton(button: FloatingActionButton, visible: Boolean){
            if (visible){
                if(button.visibility == View.INVISIBLE){
                    button.visibility = View.VISIBLE
                    button.alpha = 0f
                    button.animate().alpha(1f).setDuration(300).start()
                }
            }
            else{
                if(button.visibility == View.VISIBLE) {
                    button.animate().alpha(0f).withEndAction { button.visibility = View.INVISIBLE }
                        .setDuration(300).start()
                }
            }
        }


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
                .autoSpacing(true)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load()
        }

        fun updateSong(){
            displayPdfFromAsset("${songbook}${songNumber}.pdf")
            GlobalScope.launch(Dispatchers.IO){
                var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    numberAndTitleTV.text = "${currentSong.number}. ${currentSong.title}"
                    if(currentSong.isFavorite){
                        buttonAddToFav.isChecked = true
                    }
                    else{
                        buttonAddToFav.isChecked = false
                    }
                }
            }
        }

        try {
            updateSong()
        } catch(e: com.github.barteksc.pdfviewer.exception.FileNotFoundException) {
            Toast.makeText(this, "ERROR OPENING PDF, TRY AGAIN", Toast.LENGTH_LONG).show()
        }

        if (songNumber >= DataManager.maxSongNumber){
            animateButton(rightArrowButton, false)
        }
        else{
            animateButton(rightArrowButton, true)
        }

        if (songNumber <= 1){
            animateButton(leftArrowButton, false)
        }
        else{
            animateButton(leftArrowButton, true)
        }

        leftArrowButton.setOnClickListener{
            if(songNumber > 1) {
                songNumber--
                updateSong()
                if (songNumber < DataManager.maxSongNumber && songNumber > 1) {
                    animateButton(rightArrowButton, true)
                } else {
                    animateButton(leftArrowButton, false)
                }
            }
        }
        rightArrowButton.setOnClickListener{
            if(songNumber < DataManager.maxSongNumber) {
                songNumber++
                updateSong()
                if (songNumber > 1 && songNumber < DataManager.maxSongNumber) {
                    animateButton(leftArrowButton, true)
                } else {
                    animateButton(rightArrowButton, false)
                }
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