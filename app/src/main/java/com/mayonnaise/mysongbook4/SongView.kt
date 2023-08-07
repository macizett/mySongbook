package com.mayonnaise.mysongbook4

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
                    duration = 70L
                    withEndAction {
                        textView.text = text
                        translationX = 1f
                        animate().alpha(1f).start()
                    }
                }.start()
            }
        }

        fun animateButton(button: FloatingActionButton, visible: Boolean){
            if (visible){
                button.visibility = View.VISIBLE
                button.alpha = 0f
                button.animate().alpha(1f).setDuration(800).start()
            }
            else{
                button.animate().alpha(0f).withEndAction { button.visibility = View.INVISIBLE }.setDuration(100).start()
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

        rightArrowButton.setOnClickListener{
            if (songNumber < DataManager.maxSongNumber) {
                songNumber++
                GlobalScope.launch(Dispatchers.IO) {
                    currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    withContext(Dispatchers.Main) {
                        updateSong(currentSong, -songTV.width.toFloat())

                        if (songNumber > 1 && songNumber < DataManager.maxSongNumber) {
                            if (leftArrowButton.visibility == View.INVISIBLE) {
                                animateButton(leftArrowButton, true)
                            }
                        } else {
                            if (rightArrowButton.visibility == View.VISIBLE) {
                                animateButton(rightArrowButton, false)
                            }
                        }
                    }
                }
            }
        }

        leftArrowButton.setOnClickListener{
            if (songNumber > 1) {
                songNumber--
                GlobalScope.launch(Dispatchers.IO) {
                    currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    withContext(Dispatchers.Main) {
                        updateSong(currentSong, songTV.width.toFloat())

                        if (songNumber < DataManager.maxSongNumber && songNumber > 1) {
                            if (rightArrowButton.visibility == View.INVISIBLE) {
                                animateButton(rightArrowButton, true)
                            }
                        } else {
                            if (leftArrowButton.visibility == View.VISIBLE) {
                                animateButton(leftArrowButton, false)
                            }
                        }
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