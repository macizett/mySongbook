package com.mayonnaise.mysongbook4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.mayonnaise.mysongbook4.databinding.ActivitySearchPhraseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class SearchPhrase : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPhraseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchPhraseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        lateinit var foundSongs: List<SongEntity>
        lateinit var phrase: String

        binding.phraseInput.textSize = DataManager.textSize-3
        binding.buttonSearch.textSize = DataManager.textSize-5
        binding.infoTV.textSize = DataManager.textSize-4
        binding.foundQuantityTV.textSize = DataManager.textSize-5

        binding.phraseInput.setTypeface(null, DataManager.textStyle)
        binding.buttonSearch.setTypeface(null, DataManager.textStyle)
        binding.infoTV.setTypeface(null, DataManager.textStyle)
        binding.foundQuantityTV.setTypeface(null, DataManager.textStyle)


        val songDao = SongbookDatabase.getInstance(this).songDao()
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding.viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.95f + r * 0.05f)
        }

        binding.viewPager.setPageTransformer(compositePageTransformer)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.foundQuantityTV.text = "Znaleziono: ${position+1}/${foundSongs.size}"
            }
        })

        fun search(){
            binding.infoTV.visibility = View.GONE

            phrase = binding.phraseInput.text.toString()

            println(phrase)

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                binding.foundQuantityTV.text = "Znaleziono: 0/0"

                binding.viewPager.animate()
                    .alpha(0f)
                    .setDuration(200L)
                    .withEndAction {
                        binding.viewPager.adapter = SongViewPagerAdapter(emptyList(), lifecycleScope, applicationContext, true, "")
                        binding.viewPager.visibility = View.GONE
                        binding.infoTV.visibility = View.VISIBLE
                        binding.viewPager.animate()
                            .alpha(1f)
                            .setDuration(200L)
                            .start()
                    }
                    .start()
            }
            else{
                phrase = phrase.replace("[.,]".toRegex(), "")
                if (phrase.endsWith(" ")){
                    phrase = phrase.dropLast(1)
                }

                lifecycleScope.launch(Dispatchers.Default){
                    val searchResults = songDao.searchForPhrase(phrase, DataManager.chosenSongbook)
                    val searchResultsWithoutMarks = songDao.searchForPhraseWithoutMarks(phrase, DataManager.chosenSongbook)
                    val finalSearchResults = searchResults + searchResultsWithoutMarks
                    val finalSearchResultsWithoutDupes: List<SongEntity> = finalSearchResults.toHashSet().toList().sortedBy { it.number }
                    foundSongs = finalSearchResultsWithoutDupes

                    withContext(Dispatchers.Main) {

                        if (foundSongs.isNotEmpty()) {
                            binding.viewPager.animate()
                                .alpha(0f)
                                .setDuration(100L)
                                .withEndAction {
                                    binding.viewPager.adapter = SongViewPagerAdapter(foundSongs, lifecycleScope, applicationContext, true, phrase)
                                    binding.viewPager.visibility = View.VISIBLE
                                    binding.viewPager.animate()
                                        .alpha(1f)
                                        .setDuration(100L)
                                        .start()
                                }
                                .start()

                            Toast.makeText(this@SearchPhrase, "Znaleziono ${foundSongs.size} wyników", Toast.LENGTH_SHORT).show()
                            binding.foundQuantityTV.text = "Znaleziono: 1/${foundSongs.size}"
                        }

                        else{
                            Toast.makeText(this@SearchPhrase, "Nie znaleziono frazy", Toast.LENGTH_SHORT).show()
                            binding.foundQuantityTV.text = "Znaleziono: 0/0"

                            binding.infoTV.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        binding.buttonSearch.setOnClickListener{
            search()
        }

        binding.phraseInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
        }

        binding.phraseInput.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    search()
                    return@OnKeyListener true
                }
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
                false
        })
    }

}