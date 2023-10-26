package com.mayonnaise.mysongbook4

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mayonnaise.mysongbook4.databinding.ActivitySongViewMusicModeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        binding.numberAndTitleTV.setTypeface(null, DataManager.textStyle)


        var songNumber = DataManager.songNumber

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


        val songbook = when (DataManager.chosenSongbook){
            1 -> "duchowe"
            2 -> "wedrowiec"
            3 -> "bialy"
            else -> {
                Toast.makeText(this, "ERROR READING PDF", Toast.LENGTH_SHORT).show()}
        }

        fun displayPdfFromAsset(fileName: String) {
            binding.pdfViewSong.animate()
                .alpha(0f)
                .setDuration(200L)
                .withEndAction {
                    binding.pdfViewSong.fromAsset(fileName)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(false)
                        .autoSpacing(true)
                        .pageFling(true)
                        .pageFitPolicy(FitPolicy.WIDTH)
                        .load()
                    binding.pdfViewSong.animate()
                        .alpha(1f)
                        .setDuration(200L)
                        .start()
                }
                .start()

            binding.pdfViewSong.minZoom = 0.5F
            binding.pdfViewSong.maxZoom = 1F

        }

        fun updateSong(){
            displayPdfFromAsset("pdf/${songbook}${songNumber}.pdf")
            lifecycleScope.launch(Dispatchers.IO){
                val currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                withContext(Dispatchers.Main){
                    binding.numberAndTitleTV.animate()
                        .alpha(0f)
                        .setDuration(200L)
                        .withEndAction {
                            binding.numberAndTitleTV.text = "${currentSong.number}. ${currentSong.title}"
                            binding.numberAndTitleTV.animate()
                                .alpha(1f)
                                .setDuration(200L)
                                .start()
                        }
                        .start()

                    binding.buttonAddToFav.isChecked = currentSong.isFavorite
                }
            }
        }

        updateSong()

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

        binding.buttonAddToFav.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){
                lifecycleScope.launch(Dispatchers.IO) {
                    val currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = true
                    songDao.updateFavoriteSongs(currentSong)
                }
            }else{
                lifecycleScope.launch(Dispatchers.IO) {
                    val currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
                    currentSong.isFavorite = false
                    songDao.updateFavoriteSongs(currentSong)
                }
            }
        }

    }

}