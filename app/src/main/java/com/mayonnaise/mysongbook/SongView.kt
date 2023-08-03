package com.mayonnaise.mysongbook

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
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
        val scrollView: ScrollView = findViewById(R.id.scrollView)
        val sliderTextSize: Slider = findViewById(R.id.sliderTextSize)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        lateinit var currentSong: SongEntity

        var songNumber = DataManager.chosenSong


        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        var textSize = sharedPrefs.getFloat("textSize", 18.0F)

        sliderTextSize.value = textSize
        songTV.textSize = sliderTextSize.value
        numberAndTitleTV.textSize = sliderTextSize.value+2

        fun songFavSaving(isFav: Boolean){
            currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
            currentSong.isFavorite = isFav
            songDao.updateFavoriteSongs(currentSong)
        }

        fun animateText(textView: TextView, text: String,  side: Float){
            textView.apply {
                animate().apply {
                    translationXBy(side)
                    alpha(0f)
                    duration = 200
                    withEndAction {
                        textView.text = text
                        translationX = 1f
                        animate().alpha(1f).start()
                    }
                }.start()
            }
        }

        fun updateSong(currentSong: SongEntity, side: Float){
            animateText(numberAndTitleTV, "${currentSong.number}. ${currentSong.title}", side)
            animateText(songTV, currentSong.text, side)

            scrollView.smoothScrollTo(1, 1)

            buttonAddToFav.isChecked = currentSong.isFavorite
        }

        GlobalScope.launch(Dispatchers.IO) {
            currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
            withContext(Dispatchers.Main) {
                updateSong(currentSong, -songTV.width.toFloat())
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
                currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    updateSong(currentSong, -songTV.width.toFloat())

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
                currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    updateSong(currentSong, songTV.width.toFloat())

                    if(songNumber < DataManager.maxSongNumber && songNumber > 1){
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
                songFavSaving(true)
            }
        }else{
            GlobalScope.launch(Dispatchers.IO) {
                songFavSaving(false)
            }
        }
        }

        sliderTextSize.addOnChangeListener { slider, value, fromUser ->
            songTV.textSize = sliderTextSize.value
            numberAndTitleTV.textSize = sliderTextSize.value+2
            sharedPrefs.edit().putFloat("textSize", sliderTextSize.value).apply()
        }

    }
}