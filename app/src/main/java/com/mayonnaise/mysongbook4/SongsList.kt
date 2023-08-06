package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songslist)

        val buttonGo: Button = findViewById(R.id.buttonGo)
        val getNumber: EditText = findViewById(R.id.textInputEditText)
        val searchButton: Button = findViewById(R.id.searchButton)
        var favoritesButton: Button = findViewById(R.id.favoritesButton)
        var sortButton: Button = findViewById(R.id.sortButton)
        var recyclerViewTOC: RecyclerView = findViewById(R.id.recyclerViewTOC)

        var maxSongNr = DataManager.maxSongNumber

        lateinit var adapter: SongAdapter

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        GlobalScope.launch (Dispatchers.IO) {
            var songDao = SongbookDatabase.getInstance(applicationContext).songDao()
            var allSongs = songDao.getAllSongsBySongbook(DataManager.chosenSongbook)
            withContext(Dispatchers.Main){
                adapter = SongAdapter(allSongs)
                recyclerViewTOC.layoutManager = LinearLayoutManager(applicationContext)
                recyclerViewTOC.adapter = adapter
                if(!sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY", false)){
                    adapter.sortAlphabetically()
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.sortNumerically()
                    adapter.notifyDataSetChanged()
                }
            }
        }

        fun goToNumber(){
            if (getNumber.text.isEmpty()){
                Toast.makeText(this, "Wpisz numer pieśni!", Toast.LENGTH_SHORT).show()
            }
            else{
                var number = getNumber.text.toString().toInt()
                if(number <= maxSongNr && number > 0) {
                    DataManager.chosenSong = number

                    if(DataManager.musicMode == true){
                        val showSongViewMusicMode = Intent(this, SongViewMusicMode::class.java)
                        startActivity(showSongViewMusicMode)
                    }
                    else{
                        val showSongView = Intent(this, SongView::class.java)
                        startActivity(showSongView)
                    }
                }
                else{
                    Toast.makeText(this, "Nieprawidłowy numer pieśni!", Toast.LENGTH_SHORT).show()
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
                }
            }
        }


        buttonGo.setOnClickListener{
            goToNumber()
        }

        searchButton.setOnClickListener {
            val searchActivity = Intent(this, SearchPhrase::class.java)
            startActivity(searchActivity)
        }

        sortButton.setOnClickListener{
            if(sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY", true)){
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY", false).apply()
                adapter.sortAlphabetically()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Sortowanie alfabetyczne", Toast.LENGTH_SHORT).show()
            }
            else{
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY", true).apply()
                adapter.sortNumerically()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Sortowanie numeryczne", Toast.LENGTH_SHORT).show()
            }
        }

        favoritesButton.setOnClickListener{
            val showFavoritesActivity = Intent(this, FavoriteSongsList::class.java)
            startActivity(showFavoritesActivity)
        }

        getNumber.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.getWindowToken(), 0)
                goToNumber()
                return@OnKeyListener true
            }
            false
        })
    }
}
