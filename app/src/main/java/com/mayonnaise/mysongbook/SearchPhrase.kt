package com.mayonnaise.mysongbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
        var numberTV: TextView = findViewById(R.id.numberTV)
        var infoTV: TextView = findViewById(R.id.infoTV)

        var currentSearchedSong: Int = 0
        var currentFound: Int = 0
        var currentDisplayedSong: Int = 0

        var foundSongs = arrayListOf<Int>(0)

        arrowRightButton.setVisibility(View.GONE)
        arrowLeftButton.setVisibility(View.GONE)

        var songbook = songbook_duchowe.duchowe
        if (chosenSongStore.chosenSongbook == "duchowe"){
            songbook = songbook_duchowe.duchowe
        }
        if (chosenSongStore.chosenSongbook == "wedrowiec"){
            songbook = songbook_wedrowiec.wedrowiec
        }
        if (chosenSongStore.chosenSongbook == "bialy"){
            songbook = songbook_bialy.bialy
        }


        fun search(){

            var phrase: String = phraseInput.text.toString()

            if(phrase.isEmpty()){                                                                            //if empty
                Toast.makeText(this, "Wpisz frazę lub słowo kluczowe", Toast.LENGTH_LONG).show()
                numberTV.setText(" ")
                foundSongTV.setText(" ")
                infoTV.setVisibility(View.VISIBLE)
                arrowLeftButton.setVisibility(View.INVISIBLE)
                arrowRightButton.setVisibility(View.INVISIBLE)

            }

            else {                                                                                           //if not empty
                    if (currentSearchedSong < chosenSongStore.maxSongNumber) {

                        if (songbook[currentSearchedSong].contains(phrase, ignoreCase = true)) {         //Found
                            foundSongs.add(currentSearchedSong)
                            currentFound++
                            currentSearchedSong++
                            search()

                        }
                        else {                                                                               //Not found
                            songWithoutComma.song = songbook[currentSearchedSong].replace(",".toRegex(), "")  //Searching without commas

                            if (songWithoutComma.song.contains(phrase, ignoreCase = true)) {                 //Found
                                foundSongs.add(currentSearchedSong)
                                currentFound++
                                currentSearchedSong++
                                search()
                            }
                            else {                                                                         //Not found
                                if (currentSearchedSong < chosenSongStore.maxSongNumber) {
                                    currentSearchedSong++
                                    search()
                                }
                            }
                        }
                    }
                    else {
                        if(currentFound == 0){
                            Toast.makeText(this, "Nie znaleziono frazy", Toast.LENGTH_LONG).show()
                            numberTV.setText(" ")
                            foundSongTV.setText(" ")
                            infoTV.setVisibility(View.VISIBLE)
                            arrowLeftButton.setVisibility(View.INVISIBLE)
                            arrowRightButton.setVisibility(View.INVISIBLE)
                        }
                        else{
                            Toast.makeText(this, "Znaleziono $currentFound wyników", Toast.LENGTH_LONG).show()
                            foundSongTV.setText(songbook[foundSongs[currentDisplayedSong]])
                            numberTV.setVisibility(View.VISIBLE)
                            numberTV.setText((foundSongs[currentDisplayedSong]+1).toString())
                            textViewFoundSongs.setText("${currentDisplayedSong}/${currentFound}")
                            if(currentFound > 1){
                                arrowRightButton.setVisibility(View. VISIBLE)
                            }
                        }
                    }
            }
        }

        arrowRightButton.setOnClickListener {
            currentDisplayedSong++
            textViewFoundSongs.setText("${currentDisplayedSong}/${currentFound}")
            foundSongTV.setText(songbook[foundSongs[currentDisplayedSong]])
            numberTV.setText((foundSongs[currentDisplayedSong]+1).toString())
            if (currentDisplayedSong < foundSongs.size-1) {
                arrowRightButton.setVisibility(View.VISIBLE)
            }
            else{arrowRightButton.setVisibility(View.INVISIBLE)}
            if (currentDisplayedSong > 1) {
                arrowLeftButton.setVisibility(View.VISIBLE)
            }
            else{arrowLeftButton.setVisibility(View.INVISIBLE)}
        }

        arrowLeftButton.setOnClickListener{
            currentDisplayedSong--
            textViewFoundSongs.setText("${currentDisplayedSong}/${currentFound}")
            foundSongTV.setText(songbook[foundSongs[currentDisplayedSong]])
            numberTV.setText((foundSongs[currentDisplayedSong]+1).toString())
            if(currentDisplayedSong < foundSongs.size-1){
                arrowRightButton.setVisibility(View.VISIBLE)
            }
            else{arrowRightButton.setVisibility(View.INVISIBLE)}
            if(currentDisplayedSong > 1){
                arrowLeftButton.setVisibility(View.VISIBLE)
            }
            else{arrowLeftButton.setVisibility(View.INVISIBLE)}
        }


        searchButton.setOnClickListener{
            infoTV.setVisibility(View.GONE)
            currentSearchedSong = 0
            currentFound = 0
            currentDisplayedSong = 1
            foundSongs = arrayListOf<Int>(0)
            search()
        }

        numberTV.setOnClickListener{
            chosenSongStore.chosenSong = (foundSongs[currentDisplayedSong]+1).toString()
            val showSongView = Intent(this, SongView::class.java)
            startActivity(showSongView)
        }

    }

}