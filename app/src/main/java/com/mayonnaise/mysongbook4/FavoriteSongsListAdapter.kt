package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mayonnaise.mysongbook4.SongParser.coroutineExceptionHandler
import com.mayonnaise.mysongbook4.databinding.FavoritesViewRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class FavoriteSongsListAdapter(private var songEntities: List<SongEntity>, private var context: Context, private var lifecycleScope: LifecycleCoroutineScope,
                               private var recyclerView: RecyclerView): RecyclerView.Adapter<FavoriteSongsListAdapter.FavouritesViewHolder>() {

    inner class FavouritesViewHolder(binding: FavoritesViewRowBinding): ViewHolder(binding.root){
        val songTitleTV = binding.songTitleTV
        val songNumberTV = binding.songNumberTV
        val buttonFavourites = binding.buttonAddToFav
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favouritesRowBinding = FavoritesViewRowBinding.inflate(inflater, parent, false)
        return FavouritesViewHolder(favouritesRowBinding)
    }

    override fun getItemCount(): Int {
        return songEntities.size
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        val song = songEntities[position]

        holder.songNumberTV.text = song.number.toString()
        holder.songTitleTV.text = song.title

        holder.songNumberTV.textSize = DataManager.textSize-3
        holder.songTitleTV.textSize = DataManager.textSize-3

        holder.songTitleTV.setTypeface(null, DataManager.textStyle)


        holder.itemView.setOnClickListener{
                DataManager.chosenSong = songEntities[position].number
                if(DataManager.musicMode){
                    val intent = Intent(holder.itemView.context,SongViewMusicMode::class.java)
                    holder.itemView.context.startActivity(intent)
                }
                else{
                    val intent = Intent(holder.itemView.context,SongView::class.java)
                    holder.itemView.context.startActivity(intent)
                }

            }

            holder.buttonFavourites.setOnCheckedChangeListener{ _, isChecked ->
                if (!isChecked) {
                    song.isFavorite = false
                    lifecycleScope.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }

                } else {
                    song.isFavorite = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }
                }
            }

    }

    private fun updateSongInDatabase(song: SongEntity) {
        val songDao = SongbookDatabase.getInstance(context).songDao()

        lifecycleScope.launch(Dispatchers.IO) {
            songDao.updateFavoriteSongs(song)
        }
    }

    fun sort(sortingPreference: Boolean) {
        val collator = Collator.getInstance()
        if(!sortingPreference){
            lifecycleScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
                delay(200L)
                songEntities = songEntities.sortedBy {
                    collator.getCollationKey(it.title)
                }
                withContext(Dispatchers.Main){
                    recyclerView.animate()
                        .alpha(0f)
                        .setDuration(200L)
                        .withEndAction {
                            notifyDataSetChanged()
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.animate()
                                .alpha(1f)
                                .setDuration(200L)
                                .start()
                        }
                        .start()
                }
            }
        }
        else{
            lifecycleScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
                delay(200L)
                songEntities = songEntities.sortedBy { it.number }
                withContext(Dispatchers.Main){
                    recyclerView.animate()
                        .alpha(0f)
                        .setDuration(200L)
                        .withEndAction {
                            notifyDataSetChanged()
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.animate()
                                .alpha(1f)
                                .setDuration(200L)
                                .start()
                        }
                        .start()
                }
            }
        }
    }

}