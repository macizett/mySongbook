package com.mayonnaise.mysongbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPhrase : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_phrase)

        var phraseInput: EditText = findViewById(R.id.phraseSearch)
        var searchButton: Button = findViewById(R.id.buttonSearch)
        var arrowLeftButton: FloatingActionButton = findViewById(R.id.arrowLeft)
        var arrowRightButton: FloatingActionButton = findViewById(R.id.arrowRight)
        var foundSongTV: TextView = findViewById(R.id.foundSongTV)
        var textViewFoundSongs: TextView = findViewById(R.id.textView7)
        var numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)
        var infoTV: TextView = findViewById(R.id.infoTV)

        var currentDisplayedSong: Int = 0
        lateinit var foundSongs: List<SongEntity>


        val songDao = SongbookDatabase.getInstance(this).songDao()


        fun search(){
            var phrase: String = phraseInput.text.toString()

            arrowLeftButton.setVisibility(View.INVISIBLE)
            arrowRightButton.setVisibility(View.INVISIBLE)

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                numberAndTitleTV.setText(" ")
                foundSongTV.setText(" ")
                infoTV.setVisibility(View.VISIBLE)
                arrowLeftButton.setVisibility(View.INVISIBLE)
                arrowRightButton.setVisibility(View.INVISIBLE)

            }
            else{
                phrase = phrase.replace("[.,]".toRegex(), "")
                if (phrase.endsWith(" ")){
                    phrase = phrase.dropLast(1)
                }
                GlobalScope.launch(Dispatchers.Default){
                    var searchResults = songDao.searchForPhrase(phrase, DataManager.chosenSongbook)
                    var searchResultsWithoutMarks = songDao.searchForPhraseWithoutMarks(phrase, DataManager.chosenSongbook)
                    var finalSearchResults = searchResults + searchResultsWithoutMarks
                    var finalSearchResultsWithoutDupes: List<SongEntity> = finalSearchResults.toHashSet().toList().sortedBy { it.number }
                    foundSongs = finalSearchResultsWithoutDupes
                    withContext(Dispatchers.Main) {
                        if (foundSongs.isNotEmpty()) {
                            Toast.makeText(this@SearchPhrase, "Znaleziono ${foundSongs.size} wyników", Toast.LENGTH_LONG).show()
                            foundSongTV.setText(foundSongs[currentDisplayedSong].text)
                            numberAndTitleTV.setVisibility(View.VISIBLE)
                            numberAndTitleTV.setText("${foundSongs[currentDisplayedSong].number.toString()}. ${foundSongs[currentDisplayedSong].title}")
                            textViewFoundSongs.setText("${currentDisplayedSong+1}/${foundSongs.size}")
                            if (foundSongs.size > 1) {
                                arrowRightButton.setVisibility(View.VISIBLE)
                            }


                        }

                        else{
                            Toast.makeText(this@SearchPhrase, "Nie znaleziono frazy", Toast.LENGTH_LONG).show()
                            numberAndTitleTV.setText("")
                            foundSongTV.setText("")
                            infoTV.setVisibility(View.VISIBLE)
                            arrowLeftButton.setVisibility(View.INVISIBLE)
                            arrowRightButton.setVisibility(View.INVISIBLE)
                        }
                    }
                }
            }

        }

        arrowRightButton.setOnClickListener {
            currentDisplayedSong++
            textViewFoundSongs.setText("${currentDisplayedSong+1}/${foundSongs.size}")
            foundSongTV.setText(foundSongs[currentDisplayedSong].text)
            numberAndTitleTV.setText("${foundSongs[currentDisplayedSong].number.toString()}. ${foundSongs[currentDisplayedSong].title}")
            if (currentDisplayedSong < foundSongs.size-1) {
                arrowRightButton.setVisibility(View.VISIBLE)
            }
            else{arrowRightButton.setVisibility(View.INVISIBLE)}
            if (currentDisplayedSong > 0) {
                arrowLeftButton.setVisibility(View.VISIBLE)
            }
            else{arrowLeftButton.setVisibility(View.INVISIBLE)}
        }

        arrowLeftButton.setOnClickListener{
            currentDisplayedSong--
            textViewFoundSongs.setText("${currentDisplayedSong+1}/${foundSongs.size}")
            foundSongTV.setText(foundSongs[currentDisplayedSong].text)
            numberAndTitleTV.setText("${foundSongs[currentDisplayedSong].number.toString()}. ${foundSongs[currentDisplayedSong].title}")
            if(currentDisplayedSong < foundSongs.size-1){
                arrowRightButton.setVisibility(View.VISIBLE)
            }
            else{arrowRightButton.setVisibility(View.INVISIBLE)}
            if(currentDisplayedSong > 0){
                arrowLeftButton.setVisibility(View.VISIBLE)
            }
            else{arrowLeftButton.setVisibility(View.INVISIBLE)}
        }


        searchButton.setOnClickListener{
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
            infoTV.setVisibility(View.GONE)
            currentDisplayedSong = 0
            search()
        }

        phraseInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
                    infoTV.setVisibility(View.GONE)
                    currentDisplayedSong = 0
                    search()
                    return@OnKeyListener true
                }
                false
        })

        numberAndTitleTV.setOnClickListener{
            DataManager.chosenSong = foundSongs[currentDisplayedSong].number
            val showSongView = Intent(this, SongView::class.java)
            startActivity(showSongView)
        }

    }

}