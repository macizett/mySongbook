package com.mayonnaise.mysongbook4

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs

class SongView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songview)

        val sliderSongPicker: Slider = findViewById(R.id.sliderSongPicker)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        var songNumber = DataManager.chosenSong

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        sliderSongPicker.valueTo = DataManager.maxSongNumber.toFloat()


        viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.95f + r * 0.05f)
        }

        viewPager.setPageTransformer(compositePageTransformer)

        lifecycleScope.launch(Dispatchers.IO){
            var allSongs = SongViewPagerAdapter(songDao.getAllSongsBySongbook(DataManager.chosenSongbook), applicationContext)
            withContext(Dispatchers.Main){
                viewPager.adapter = allSongs
                viewPager.setCurrentItem(songNumber-1, false)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                lifecycleScope.launch(Dispatchers.IO){
                    var currentSong = songDao.getSongByNumber(viewPager.currentItem+1, DataManager.chosenSongbook)

                    withContext(Dispatchers.Main){
                        sliderSongPicker.value = currentSong.number.toFloat()
                    }
                }

            }
        })

        sliderSongPicker.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                    songNumber = sliderSongPicker.value.toInt()-1
                    viewPager.setCurrentItem(songNumber, true)
                    songNumber++
            }
        })
    }
}
