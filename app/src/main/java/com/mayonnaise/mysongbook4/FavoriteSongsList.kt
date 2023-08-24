package com.mayonnaise.mysongbook4

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteSongsList : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_songs_list)

        var recyclerViewFavs: RecyclerView = findViewById(R.id.recyclerViewFavorites)
        var infoTV: TextView = findViewById(R.id.infoTV)
        var labelTV: TextView = findViewById(R.id.labelTV)
        var sortButton: Button = findViewById(R.id.sortButton)

        infoTV.textSize = DataManager.textSize-4
        labelTV.textSize = DataManager.textSize

        lateinit var adapter: FavoritesAdapter

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        lifecycleScope.launch (Dispatchers.Default) {
        var songDao = SongbookDatabase.getInstance(applicationContext).songDao()
            var favoriteSongs = songDao.getFavoriteSongs(DataManager.chosenSongbook)
            withContext(Dispatchers.Main){
            if(favoriteSongs.isNotEmpty()){
                infoTV.visibility = View.GONE
            }
            else{
                infoTV.visibility = View.VISIBLE
            }
            adapter = FavoritesAdapter(favoriteSongs, applicationContext, lifecycleScope)
            recyclerViewFavs.layoutManager = LinearLayoutManager(applicationContext)
            recyclerViewFavs.adapter = adapter


                if(!sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY_FAVS", false)){
                    adapter.sortAlphabetically()
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.sortNumerically()
                    adapter.notifyDataSetChanged()
                }}
        }

        sortButton.setOnClickListener{
            if(sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY_FAVS", true)){
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY_FAVS", false).apply()
                adapter.sortAlphabetically()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Sortowanie alfabetyczne", Toast.LENGTH_SHORT).show()
            }
            else{
                sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY_FAVS", true).apply()
                adapter.sortNumerically()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Sortowanie numeryczne", Toast.LENGTH_SHORT).show()
            }
        }

    }

    }
