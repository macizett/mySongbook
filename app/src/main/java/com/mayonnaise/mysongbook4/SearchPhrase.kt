package com.mayonnaise.mysongbook4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.mayonnaise.mysongbook4.databinding.ActivitySearchPhraseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


        val songDao = SongbookDatabase.getInstance(this).songDao()

        binding.viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
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
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
            binding.infoTV.visibility = View.GONE

            phrase = binding.phraseInput.text.toString()

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                binding.foundQuantityTV.text = "Znaleziono: 0/0"

                binding.infoTV.setVisibility(View.VISIBLE)
            }
            else{
                phrase = phrase.replace("[.,]".toRegex(), "")
                if (phrase.endsWith(" ")){
                    phrase = phrase.dropLast(1)
                }

                lifecycleScope.launch(Dispatchers.Default){
                    var searchResults = songDao.searchForPhrase(phrase, DataManager.chosenSongbook)
                    var searchResultsWithoutMarks = songDao.searchForPhraseWithoutMarks(phrase, DataManager.chosenSongbook)
                    var finalSearchResults = searchResults + searchResultsWithoutMarks
                    var finalSearchResultsWithoutDupes: List<SongEntity> = finalSearchResults.toHashSet().toList().sortedBy { it.number }
                    foundSongs = finalSearchResultsWithoutDupes

                    withContext(Dispatchers.Main) {
                        if (foundSongs.isNotEmpty()) {
                            Toast.makeText(this@SearchPhrase, "Znaleziono ${foundSongs.size} wyników", Toast.LENGTH_SHORT).show()
                            binding.viewPager.adapter = SongViewPagerAdapter(foundSongs, lifecycleScope, applicationContext, true, phrase)
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

        binding.phraseInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    search()
                    return@OnKeyListener true
                }
                false
        })
    }

}