package com.mayonnaise.mysongbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
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

        lateinit var adapter: FavoritesAdapter

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
            adapter = FavoritesAdapter(favoriteSongs, applicationContext)
            recyclerViewFavs.layoutManager = LinearLayoutManager(applicationContext)
            recyclerViewFavs.adapter = adapter}
        }

    }

    }
