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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPhrase : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phrase)

        var phraseInput: EditText = findViewById(R.id.phraseSearch)
        var searchButton: Button = findViewById(R.id.buttonSearch)
        var viewPager: ViewPager2 = findViewById(R.id.view_pager)
        var foundSongsTV: TextView = findViewById(R.id.foundQuantity)
        var infoTV: TextView = findViewById(R.id.infoTV)

        lateinit var foundSongs: List<SongEntity>
        lateinit var phrase: String

        phraseInput.textSize = DataManager.textSize-3
        searchButton.textSize = DataManager.textSize-5
        infoTV.textSize = DataManager.textSize-4
        foundSongsTV.textSize = DataManager.textSize-5


        val songDao = SongbookDatabase.getInstance(this).songDao()

        viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
        }

        val compositePageTransformer = CompositePageTransformer()

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = (0.95f + r * 0.05f)
        }

        viewPager.setPageTransformer(compositePageTransformer)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                foundSongsTV.text = "Znaleziono: ${position+1}/${foundSongs.size}"
            }
        })

        fun search(){
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
            infoTV.visibility = View.GONE

            phrase = phraseInput.text.toString()

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                foundSongsTV.text = "Znaleziono: 0/0"

                infoTV.setVisibility(View.VISIBLE)
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
                            viewPager.adapter = SongViewPagerAdapter(foundSongs, applicationContext, true, phrase)
                            foundSongsTV.text = "Znaleziono: 1/${foundSongs.size}"
                        }

                        else{
                            Toast.makeText(this@SearchPhrase, "Nie znaleziono frazy", Toast.LENGTH_SHORT).show()
                            foundSongsTV.text = "Znaleziono: 0/0"

                            infoTV.setVisibility(View.VISIBLE)
                        }
                    }
                }
            }
        }

        searchButton.setOnClickListener{
            search()
        }

        phraseInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    search()
                    return@OnKeyListener true
                }
                false
        })
    }

}