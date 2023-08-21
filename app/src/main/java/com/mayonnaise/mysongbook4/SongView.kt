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

        val numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)
        val buttonAddToFav: CheckBox = findViewById(R.id.buttonAddToFav)
        val sliderSongPicker: Slider = findViewById(R.id.sliderSongPicker)
        var isUserInteraction = false

        val songDao = SongbookDatabase.getInstance(this).songDao()

        lateinit var currentSong: SongEntity

        var songNumber = DataManager.chosenSong


        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE
            )
        }

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).overScrollMode =
                RecyclerView.OVER_SCROLL_NEVER
        }

        GlobalScope.launch(Dispatchers.IO){
            var allSongs = SongViewPagerAdapter(songDao.getAllSongsBySongbook(DataManager.chosenSongbook), applicationContext)
            withContext(Dispatchers.Main){
                viewPager.adapter = allSongs
                viewPager.setCurrentItem(songNumber-1, false)
            }
        }

        sliderSongPicker.valueTo = DataManager.maxSongNumber.toFloat()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((10 * Resources.getSystem().displayMetrics.density).toInt()))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.80f + r * 0.20f)
        }

        viewPager.setPageTransformer(compositePageTransformer)

        viewPager.post {

        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                GlobalScope.launch(Dispatchers.IO){
                    var currentSong = songDao.getSongByNumber(viewPager.currentItem+1, DataManager.chosenSongbook)
                    songNumber = viewPager.currentItem+1

                    withContext(Dispatchers.Main){
                               /* numberAndTitleTV.apply {
                                    animate().apply {
                                        alpha(0f)
                                        duration = 50L
                                        withEndAction {
                                            text = "${currentSong.number}. ${currentSong.title}"
                                            animate().alpha(1f).start()
                                        }
                                    }.start()
                                }*/
                            numberAndTitleTV.text = "${currentSong.number}. ${currentSong.title}"
                            if(currentSong.isFavorite){
                                buttonAddToFav.isChecked = true
                            }
                            else{
                                buttonAddToFav.isChecked = false
                            }
                        isUserInteraction = false
                        sliderSongPicker.value = currentSong.number.toFloat()
                        isUserInteraction = true
                    }
                }

            }
        })

        /* var textSize = sharedPrefs.getFloat("textSize", 18.0F)

        sliderSongPicker.value = textSize
        songTV.textSize = sliderSongPicker.value
        numberAndTitleTV.textSize = sliderSongPicker.value+2*/

        fun songFavSaving(isFav: Boolean) {
            currentSong = songDao.getSongByNumber(songNumber, DataManager.chosenSongbook)
            currentSong.isFavorite = isFav
            songDao.updateFavoriteSongs(currentSong)
        }

        buttonAddToFav.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                GlobalScope.launch(Dispatchers.IO) {
                    songFavSaving(true)
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    songFavSaving(false)
                }
            }
        }

        sliderSongPicker.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                if (fromUser && isUserInteraction) {
                    isUserInteraction = true
                    songNumber = sliderSongPicker.value.toInt()-1
                    viewPager.setCurrentItem(songNumber, true)
                    songNumber++
                }
                else{
                    isUserInteraction = false
                }
            }
        })
    }
}
