package com.mayonnaise.mysongbook4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.slider.Slider
import com.mayonnaise.mysongbook4.databinding.ActivitySongviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongView : AppCompatActivity() {

    private lateinit var binding: ActivitySongviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        var songNumber = DataManager.chosenSong

        binding.sliderSongPicker.valueTo = DataManager.maxSongNumber.toFloat()

        binding.sliderSongPicker

        binding.viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = (0.95f + r * 0.05f)
        }

        binding.viewPager.setPageTransformer(compositePageTransformer)

        lifecycleScope.launch(Dispatchers.IO){
            val allSongs = SongViewPagerAdapter(songDao.getAllSongsBySongbook(DataManager.chosenSongbook), lifecycleScope, applicationContext, false, "")
            withContext(Dispatchers.Main){
                binding.viewPager.adapter = allSongs
                binding.viewPager.setCurrentItem(songNumber-1, false)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                lifecycleScope.launch(Dispatchers.IO){
                    val currentSong = songDao.getSongByNumber(binding.viewPager.currentItem+1, DataManager.chosenSongbook)

                    withContext(Dispatchers.Main){
                        binding.sliderSongPicker.value = currentSong.number.toFloat()
                    }
                }

            }
        })

        binding.sliderSongPicker.addOnChangeListener(Slider.OnChangeListener { _, value, _ -> songNumber = value.toInt()-1 })


        binding.sliderSongPicker.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                binding.viewPager.animate()
                    .alpha(0f)
                    .setDuration(200L)
                    .withEndAction {
                        binding.viewPager.setCurrentItem(songNumber, false)
                        binding.viewPager.animate()
                            .alpha(1f)
                            .setDuration(200L)
                            .start()
                    }
                    .start()
            }
        })
    }
}
