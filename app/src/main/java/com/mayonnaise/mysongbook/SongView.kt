package com.mayonnaise.mysongbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songview)

        val songTV: TextView = findViewById(R.id.songTV)
        val numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)
        val leftArrowButton: FloatingActionButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: FloatingActionButton = findViewById(R.id.rightArrowButton)
        val buttonAddToFav: CheckBox = findViewById(R.id.buttonAddToFav)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        var songNumber = DataManager.chosenSong

        fun checkSong(currentSong: SongEntity){
            if (currentSong.isFavorite){
                buttonAddToFav.isChecked = true
            }
            else{
                buttonAddToFav.isChecked = false
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
            withContext(Dispatchers.Main) {
                numberAndTitleTV.setText("${currentSong.number}. ${currentSong.title}")
                songTV.setText(currentSong.text)
                checkSong(currentSong)
            }
        }

        if (songNumber >= DataManager.maxSongNumber-1){
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

        rightArrowButton.setOnClickListener{
            songNumber++
            GlobalScope.launch(Dispatchers.IO) {
                var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    songTV.setText(currentSong.text)
                    checkSong(currentSong)
                    numberAndTitleTV.setText("${currentSong.number}. ${currentSong.title}")
                    if(songNumber > 1 && songNumber < DataManager.maxSongNumber-1){
                        leftArrowButton.visibility = View.VISIBLE
                    }
                    else{
                        rightArrowButton.visibility = View.INVISIBLE
                    }
                }
            }
        }

        leftArrowButton.setOnClickListener{
            songNumber--
            GlobalScope.launch(Dispatchers.IO) {
                var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    songTV.setText(currentSong.text)
                    checkSong(currentSong)
                    numberAndTitleTV.setText("${currentSong.number}. ${currentSong.title}")
                    if(songNumber < DataManager.maxSongNumber-1 && songNumber > 1){
                        rightArrowButton.visibility = View.VISIBLE
                    }
                    else{
                        leftArrowButton.visibility = View.INVISIBLE
                    }
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