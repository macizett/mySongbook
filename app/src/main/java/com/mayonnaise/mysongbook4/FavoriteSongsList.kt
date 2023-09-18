package com.mayonnaise.mysongbook4

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mayonnaise.mysongbook4.databinding.ActivityFavoriteSongsListBinding
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteSongsList : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteSongsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteSongsListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.infoTV.textSize = DataManager.textSize-4
        binding.labelTV.textSize = DataManager.textSize

        binding.infoTV.setTypeface(null, DataManager.textStyle)

        lateinit var adapter: FavoriteSongsListAdapter

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        lifecycleScope.launch (Dispatchers.Default) {
        val songDao = SongbookDatabase.getInstance(applicationContext).songDao()
            val favoriteSongs = songDao.getFavoriteSongs(DataManager.chosenSongbook)
            withContext(Dispatchers.Main){
            if(favoriteSongs.isNotEmpty()){
                binding.infoTV.visibility = View.GONE
            }
            else{
                binding.infoTV.visibility = View.VISIBLE
            }
            adapter = FavoriteSongsListAdapter(favoriteSongs, applicationContext, lifecycleScope, binding.recyclerViewFavorites)
            binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(applicationContext)

            binding.recyclerViewFavorites.adapter = adapter

            if(!sharedPrefs.getBoolean("SORTING_PREFERENCE_KEY_FAVS", false)){
                binding.switchSorting.checked = IconSwitch.Checked.LEFT
                adapter.sort(false)
            }
            else{
                binding.switchSorting.checked = IconSwitch.Checked.RIGHT
                adapter.sort(true)
            }}
        }

        binding.switchSorting.setCheckedChangeListener{ current ->

            when (current) {
                IconSwitch.Checked.LEFT -> {
                    sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY_FAVS", false).apply()
                    adapter.sort(false)
                }

                IconSwitch.Checked.RIGHT -> {
                    sharedPrefs.edit().putBoolean("SORTING_PREFERENCE_KEY_FAVS", true).apply()
                    adapter.sort(true)
                }

                else -> Toast.makeText(this, "ERROR 2137", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
