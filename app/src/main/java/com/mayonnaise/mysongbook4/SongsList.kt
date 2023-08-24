package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mayonnaise.mysongbook4.databinding.ActivitySongslistBinding
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsList : AppCompatActivity() {

    private lateinit var binding: ActivitySongslistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongslistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val maxSongNr = DataManager.maxSongNumber

        lateinit var adapter: SongAdapter

        binding.sortButton.textSize = DataManager.textSize-5
        binding.tocTV.textSize = DataManager.textSize
        binding.getNumber.textSize = DataManager.textSize-3
        binding.favoritesButton.textSize = DataManager.textSize-6
        binding.searchButton.textSize = DataManager.textSize-6

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        if(sharedPrefs.getBoolean("musicSwitchStatement", false)){
            binding.switchMusicMode.checked = IconSwitch.Checked.RIGHT
            DataManager.musicMode = true

        }
        else{
            binding.switchMusicMode.checked = IconSwitch.Checked.LEFT
            DataManager.musicMode = false
        }

        lifecycleScope.launch (Dispatchers.IO) {
            val songDao = SongbookDatabase.getInstance(applicationContext).songDao()
            val allSongs = songDao.getAllSongsBySongbook(DataManager.chosenSongbook)
            withContext(Dispatchers.Main){
                adapter = SongAdapter(allSongs, lifecycleScope)
                binding.recyclerViewTOC.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerViewTOC.adapter = adapter
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
            if (binding.getNumber.text!!.isEmpty()){
                Toast.makeText(this, "Wpisz numer pieśni!", Toast.LENGTH_SHORT).show()
            }
            else{
                val number =  binding.getNumber.text.toString().toInt()
                if(number in 1..maxSongNr) {
                    DataManager.chosenSong = number

                    if(DataManager.musicMode){
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
                    inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
                }
            }
        }


        binding.buttonGo.setOnClickListener{
            goToNumber()
        }

        binding.searchButton.setOnClickListener {
            val searchActivity = Intent(this, SearchPhrase::class.java)
            startActivity(searchActivity)
        }

        binding.sortButton.setOnClickListener{
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

        binding.favoritesButton.setOnClickListener{
            val showFavoritesActivity = Intent(this, FavoriteSongsList::class.java)
            startActivity(showFavoritesActivity)
        }

        binding.getNumber.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
                goToNumber()
                return@OnKeyListener true
            }
            false
        })

        binding.switchMusicMode.setCheckedChangeListener { current ->
            when (current) {
                IconSwitch.Checked.LEFT -> {
                    sharedPrefs.edit().putBoolean("musicSwitchStatement", false).apply()
                    DataManager.musicMode = false
                    Toast.makeText(this@SongsList, "Tryb standardowy", Toast.LENGTH_SHORT).show()
                }

                IconSwitch.Checked.RIGHT -> {
                    sharedPrefs.edit().putBoolean("musicSwitchStatement", true).apply()
                    DataManager.musicMode = true
                    Toast.makeText(this@SongsList, "Tryb muzyczny", Toast.LENGTH_SHORT).show()
                }

                else -> Toast.makeText(this@SongsList, "ERROR 2137", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
