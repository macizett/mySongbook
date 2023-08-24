package com.mayonnaise.mysongbook4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.slider.Slider
import com.mayonnaise.mysongbook4.databinding.ActivitySongViewMusicModeBinding
import kotlinx.coroutines.withContext

class SongViewMusicMode : AppCompatActivity() {

    private lateinit var binding: ActivitySongViewMusicModeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongViewMusicModeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        binding.numberAndTitleTV.textSize = DataManager.textSize

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
            binding.pdfViewSong.fromAsset(fileName)
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
                    binding.numberAndTitleTV.text = "${currentSong.number}. ${currentSong.title}"
                    if(currentSong.isFavorite){
                        binding.buttonAddToFav.isChecked = true
                    }
                    else{
                        binding.buttonAddToFav.isChecked = false
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
            animateButton(binding.rightArrowButton, false)
        }
        else{
            animateButton(binding.rightArrowButton, true)
        }

        if (songNumber <= 1){
            animateButton(binding.leftArrowButton, false)
        }
        else{
            animateButton(binding.leftArrowButton, true)
        }

        binding.leftArrowButton.setOnClickListener{
            if(songNumber > 1) {
                songNumber--
                updateSong()
                if (songNumber < DataManager.maxSongNumber && songNumber > 1) {
                    animateButton(binding.rightArrowButton, true)
                } else {
                    animateButton(binding.leftArrowButton, false)
                }
            }
        }

        binding.rightArrowButton.setOnClickListener{
            if(songNumber < DataManager.maxSongNumber) {
                songNumber++
                updateSong()
                if (songNumber > 1 && songNumber < DataManager.maxSongNumber) {
                    animateButton(binding.leftArrowButton, true)
                } else {
                    animateButton(binding.rightArrowButton, false)
                }
            }
        }

        binding.buttonAddToFav.setOnCheckedChangeListener{buttonView, isChecked ->
            if (isChecked){
                lifecycleScope.launch(Dispatchers.IO) {
                    var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = true
                    songDao.updateFavoriteSongs(currentSong)
                }
            }else{
                lifecycleScope.launch(Dispatchers.IO) {
                    var currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = false
                    songDao.updateFavoriteSongs(currentSong)
                }
            }
        }

    }

}