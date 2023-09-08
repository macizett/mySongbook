package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mayonnaise.mysongbook4.databinding.ActivitySongslistBinding
import com.polyak.iconswitch.BuildConfig
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

        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding.sortButton.textSize = DataManager.textSize-5
        binding.tocTV.textSize = DataManager.textSize
        binding.getNumber.textSize = DataManager.textSize-3
        binding.favoritesButton.textSize = DataManager.textSize-6
        binding.searchButton.textSize = DataManager.textSize-6

        binding.sortButton.setTypeface(null, DataManager.textStyle)
        binding.tocTV.setTypeface(null, DataManager.textStyle)
        binding.getNumber.setTypeface(null, DataManager.textStyle)
        binding.favoritesButton.setTypeface(null, DataManager.textStyle)
        binding.searchButton.setTypeface(null, DataManager.textStyle)

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        var sortingPreference = sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY", true)

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
                adapter = SongAdapter(allSongs, lifecycleScope, binding.recyclerViewTOC, binding.progressBar!!, this@SongsList)
                binding.recyclerViewTOC.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerViewTOC.adapter = adapter
                adapter.sort(sortingPreference)
            }
        }

        binding.buttonGo.setOnClickListener{
            if (binding.getNumber.text!!.isEmpty()){
                Toast.makeText(this, "Wpisz numer pieśni!", Toast.LENGTH_SHORT).show()
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
            }
            else{
                val number =  binding.getNumber.text.toString().toInt()
                if(number in 1..maxSongNr) {
                    adapter.goToNumber(number)
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
                else{
                    Toast.makeText(this, "Nieprawidłowy numer pieśni!", Toast.LENGTH_SHORT).show()
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
            }
        }

        binding.searchButton.setOnClickListener {
            val searchActivity = Intent(this, SearchPhrase::class.java)
            startActivity(searchActivity)
        }

        binding.sortButton.setOnClickListener{
            if(sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY", true)){
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY", false).apply()
                sortingPreference = false
                adapter.sort(sortingPreference)
                Toast.makeText(this, "Sortowanie alfabetyczne", Toast.LENGTH_SHORT).show()
            }
            else{
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY", true).apply()
                sortingPreference = true
                adapter.sort(sortingPreference)
                Toast.makeText(this, "Sortowanie numeryczne", Toast.LENGTH_SHORT).show()
            }
        }

        binding.favoritesButton.setOnClickListener{
            val showFavoritesActivity = Intent(this, FavoriteSongsList::class.java)
            startActivity(showFavoritesActivity)
        }

        binding.getNumber.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (binding.getNumber.text!!.isEmpty()){
                    Toast.makeText(this, "Wpisz numer pieśni!", Toast.LENGTH_SHORT).show()
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                }
                else{
                    val number =  binding.getNumber.text.toString().toInt()
                    if(number in 1..maxSongNr) {
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                        adapter.goToNumber(number)
                    }
                    else{
                        Toast.makeText(this, "Nieprawidłowy numer pieśni!", Toast.LENGTH_SHORT).show()
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)
                    }
                }
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
