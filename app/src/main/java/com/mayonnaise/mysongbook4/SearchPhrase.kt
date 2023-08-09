package com.mayonnaise.mysongbook4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
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
        var leftArrowButton: FloatingActionButton = findViewById(R.id.arrowLeft)
        var rightArrowButton: FloatingActionButton = findViewById(R.id.arrowRight)
        var foundSongTV: TextView = findViewById(R.id.foundSongTV)
        var textViewFoundSongs: TextView = findViewById(R.id.textView7)
        var numberAndTitleTV: TextView = findViewById(R.id.numberAndTitleTV)
        var infoTV: TextView = findViewById(R.id.infoTV)
        var scrollView: ScrollView = findViewById(R.id.scrollView)

        var currentDisplayedSong: Int = 0
        lateinit var foundSongs: List<SongEntity>
        lateinit var phrase: String


        val songDao = SongbookDatabase.getInstance(this).songDao()

        fun isWordBoundary(text: String, index: Int): Boolean {
            return index == 0 || index == text.length || !Character.isLetterOrDigit(text[index - 1]) || !Character.isLetterOrDigit(text[index])
        }

        fun animateText(textView: TextView, text: String,  side: Float){
            textView.apply {
                animate().apply {
                    translationXBy(side)
                    alpha(0f)
                    duration = 70L
                    withEndAction {
                        textView.text = text
                        translationX = 1f
                        animate().alpha(1f).start()
                    }
                }.start()
            }
        }

        fun animateButton(button: FloatingActionButton, visible: Boolean){
            if (visible){
                button.visibility = View.VISIBLE
                button.alpha = 0f
                button.animate().alpha(1f).setDuration(300).start()
            }
            else{
                button.animate().alpha(0f).withEndAction { button.visibility = View.INVISIBLE }.setDuration(100).start()
            }
        }

        fun scrollScrollViewToHighlightedWord(spannable: SpannableStringBuilder, originalText: String) {
            val highlightSpans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
            if (highlightSpans.isNotEmpty()) {
                val firstHighlightedSpan = highlightSpans[0]
                val startIndex = spannable.getSpanStart(firstHighlightedSpan)

                foundSongTV.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        foundSongTV.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val layout = foundSongTV.layout
                        val line = layout.getLineForOffset(startIndex)
                        val y = layout.getLineTop(line)

                        scrollView.smoothScrollTo(0, y)
                    }
                })
            }
        }

        fun updateSong(side: Float) {

            val originalText = foundSongs[currentDisplayedSong].text
            val wordsToHighlight = phrase.split(" ")
            val spannable = SpannableStringBuilder(originalText)

            for (word in wordsToHighlight) {
                var startIndex = originalText.indexOf(word, 0, true)

                while (startIndex != -1) {

                    val endIndex = startIndex + word.length
                    if (isWordBoundary(originalText, startIndex) && isWordBoundary(originalText, endIndex)) {

                        spannable.setSpan(
                            ForegroundColorSpan(resources.getColor(R.color.foundphrase)),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    startIndex = originalText.indexOf(word, startIndex + word.length, true)
                }
            }

            foundSongTV.apply {
                animate().apply {
                    translationXBy(side)
                    alpha(0f)
                    duration = 70L
                    withEndAction {
                        foundSongTV.text = spannable
                        translationX = 1f
                        animate().alpha(1f).start()
                    }
                }.start()
            }

            animateText(numberAndTitleTV, "${foundSongs[currentDisplayedSong].number}. ${foundSongs[currentDisplayedSong].title}", side)
            textViewFoundSongs.text = "${currentDisplayedSong + 1}/${foundSongs.size}"
            scrollScrollViewToHighlightedWord(spannable, foundSongs[currentDisplayedSong].text)
        }


        fun reset(){
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
            infoTV.setVisibility(View.GONE)
            currentDisplayedSong = 0
        }

        fun search(){
            phrase = phraseInput.text.toString()

            leftArrowButton.setVisibility(View.INVISIBLE)
            rightArrowButton.setVisibility(View.INVISIBLE)

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                animateText(numberAndTitleTV, " ", -foundSongTV.width.toFloat())
                animateText(foundSongTV, " ", -foundSongTV.width.toFloat())
                textViewFoundSongs.text = "0/0"

                infoTV.setVisibility(View.VISIBLE)
                leftArrowButton.setVisibility(View.INVISIBLE)
                rightArrowButton.setVisibility(View.INVISIBLE)

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
                            numberAndTitleTV.setVisibility(View.VISIBLE)

                            updateSong(-foundSongTV.width.toFloat())

                            if (foundSongs.size > 1) {
                                rightArrowButton.setVisibility(View.VISIBLE)
                            }


                        }

                        else{
                            Toast.makeText(this@SearchPhrase, "Nie znaleziono frazy", Toast.LENGTH_LONG).show()
                            animateText(numberAndTitleTV, " ", -foundSongTV.width.toFloat())
                            animateText(foundSongTV, " ", -foundSongTV.width.toFloat())
                            textViewFoundSongs.text = "0/0"

                            infoTV.setVisibility(View.VISIBLE)
                            leftArrowButton.setVisibility(View.INVISIBLE)
                            rightArrowButton.setVisibility(View.INVISIBLE)
                        }
                    }
                }
            }

        }

        rightArrowButton.setOnClickListener {
            if(currentDisplayedSong < foundSongs.size-1) {
                currentDisplayedSong++
                updateSong(-foundSongTV.width.toFloat())

                if (currentDisplayedSong < foundSongs.size - 1) {
                    if (leftArrowButton.visibility == View.INVISIBLE) {
                        animateButton(leftArrowButton, true)
                    }
                } else {
                    if (rightArrowButton.visibility == View.VISIBLE) {
                        animateButton(rightArrowButton, false)
                    }
                }
            }
        }

        leftArrowButton.setOnClickListener{
            if(currentDisplayedSong > 0) {
                currentDisplayedSong--
                updateSong(foundSongTV.width.toFloat())

                if (currentDisplayedSong > 1) {
                    if (rightArrowButton.visibility == View.INVISIBLE) {
                        animateButton(rightArrowButton, true)
                    }
                } else {
                    if (leftArrowButton.visibility == View.VISIBLE) {
                        animateButton(leftArrowButton, false)
                    }
                }
            }
        }


        searchButton.setOnClickListener{
            reset()
            search()
        }

        phraseInput.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    reset()
                    search()
                    return@OnKeyListener true
                }
                false
        })

        numberAndTitleTV.setOnClickListener{
            DataManager.chosenSong = foundSongs[currentDisplayedSong].number
            if(DataManager.musicMode == true){
                val showSongViewMusicMode = Intent(this, SongViewMusicMode::class.java)
                startActivity(showSongViewMusicMode)
            }
            else{
                val showSongView = Intent(this, SongView::class.java)
                startActivity(showSongView)
            }
        }

    }

}