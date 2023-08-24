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
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.polyak.iconswitch.IconSwitch
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
        val musicModeSwitch: IconSwitch = findViewById(R.id.switchMusicMode)
        val tocTV: TextView = findViewById(R.id.textView)

        var maxSongNr = DataManager.maxSongNumber

        lateinit var adapter: SongAdapter
        var musicSwitch: Boolean = false

        sortButton.textSize = DataManager.textSize-5
        tocTV.textSize = DataManager.textSize
        getNumber.textSize = DataManager.textSize-3
        favoritesButton.textSize = DataManager.textSize-6
        searchButton.textSize = DataManager.textSize-6

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        if(sharedPrefs.getBoolean("musicSwitchStatement", false)){
            musicSwitch = true
            musicModeSwitch.setChecked(IconSwitch.Checked.RIGHT)
            DataManager.musicMode = true

        }
        else{
            musicSwitch = false
            musicModeSwitch.setChecked(IconSwitch.Checked.LEFT)
            DataManager.musicMode = false
        }

        lifecycleScope.launch (Dispatchers.IO) {
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

        musicModeSwitch.setCheckedChangeListener(object : IconSwitch.CheckedChangeListener {
            override fun onCheckChanged(current: IconSwitch.Checked?) {
                when (current) {
                    IconSwitch.Checked.LEFT -> {musicSwitch = false
                        sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                        DataManager.musicMode = false
                        Toast.makeText(this@SongsList, "Tryb standardowy", Toast.LENGTH_SHORT).show()}

                    IconSwitch.Checked.RIGHT -> {musicSwitch = true
                        sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                        DataManager.musicMode = true
                        Toast.makeText(this@SongsList, "Tryb muzyczny", Toast.LENGTH_SHORT).show()}

                    else -> Toast.makeText(this@SongsList, "ERROR 2137", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }
}
