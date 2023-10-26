package com.mayonnaise.mysongbook4

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.mayonnaise.mysongbook4.databinding.ActivitySongviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongView : AppCompatActivity() {

    private lateinit var binding: ActivitySongviewBinding
    private var mediaPlayer = MediaPlayer()

    var isPlaying = false
    var currentPosition = 0
    var voicesList = mutableListOf<Int>()
    val mediaPlayers = mutableListOf<MediaPlayer>()
    lateinit var currentSong: SongEntity

    var songbookPatch = when (DataManager.chosenSongbook) {
        1 -> "duchowe"
        2 -> "wedrowiec"
        3 -> "bialy"
        else -> "nothing"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val songDao = SongbookDatabase.getInstance(this).songDao()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        var songNumber = DataManager.songNumber

        binding.getNumber.textSize = DataManager.textSize - 3
        binding.buttonGo.textSize = DataManager.textSize - 6

        fun goToNumber() {
            binding.getNumberLayout.error = null
            var previousSongNumber = songNumber
            var number = binding.getNumber.text.toString()

            if (number.isNotEmpty()) {
                if (number.toInt() < DataManager.maxSongNumber && number.toInt() > 0) {
                    songNumber = number.toInt()

                    var side = when {
                        songNumber > previousSongNumber -> binding.viewPager.width.toFloat()
                        songNumber < previousSongNumber -> -binding.viewPager.width.toFloat()
                        else -> 0F
                    }

                    binding.getNumber.text?.clear()
                    inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)

                    binding.viewPager.animate()
                        .translationX(-side) // Move the ViewPager to the left
                        .setDuration(150L)
                        .withEndAction {
                            binding.viewPager.setCurrentItem(
                                songNumber - 1,
                                false
                            ) // Set the new page
                            binding.viewPager.translationX = side
                            binding.viewPager.animate()
                                .translationX(0f) // Animate back to its original position
                                .setDuration(150L)
                                .start()
                        }
                        .start()

                } else {
                    binding.getNumberLayout.error = getString(R.string.errorNumber)
                }
            } else {
                binding.getNumberLayout.error = getString(R.string.errorNumberEmpty)
            }
        }

        binding.viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        binding.getNumber.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = (0.95f + r * 0.05f)
        }

        binding.viewPager.setPageTransformer(compositePageTransformer)

        lifecycleScope.launch(Dispatchers.IO) {
            val allSongs = SongViewPagerAdapter(
                songDao.getAllSongsBySongbook(DataManager.chosenSongbook),
                lifecycleScope,
                applicationContext,
                false,
                ""
            )
            withContext(Dispatchers.Main) {
                binding.viewPager.animate()
                    .alpha(0f)
                    .setDuration(100L)
                    .withEndAction {
                        binding.viewPager.adapter = allSongs
                        binding.viewPager.setCurrentItem(songNumber - 1, false)
                        binding.viewPager.animate()
                            .alpha(1f)
                            .setDuration(100L)
                            .start()
                    }
                    .start()
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mediaPlayer?.stop()

                DataManager.songNumber = binding.viewPager.currentItem + 1
                currentPosition = 0
                isPlaying = false
                lifecycleScope.launch(Dispatchers.IO) {
                    var song =
                        songDao.getSongByNumber(DataManager.songNumber, DataManager.chosenSongbook)
                    currentSong = song
                }
            }
        })

        binding.buttonGo.setOnClickListener {
            goToNumber()
        }

        binding.buttonShare.setOnClickListener {
            val sharedText = """
${DataManager.chosenSongbookName}
                        
${currentSong.text}

_________________________________
   
https://www.ksiegarniarlch.pl/
                
- wysłano z aplikacji mySongbook.
""".trimIndent()

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sharedText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }


        binding.buttonReport.setOnClickListener {
            DataManager.isSongReported = true
            val sendReportActivity = Intent(this, SendReportActivity::class.java)
            this.startActivity(sendReportActivity)
        }

        binding.getNumber.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
                goToNumber()
                return@OnKeyListener true
            }
            false
        })
    }
}
