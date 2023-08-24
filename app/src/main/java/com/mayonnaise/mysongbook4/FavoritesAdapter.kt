package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mayonnaise.mysongbook4.SongParser.coroutineExceptionHandler
import com.mayonnaise.mysongbook4.databinding.FavoritesViewRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class FavoritesAdapter(private var songEntities: List<SongEntity>, context: Context, private var lifecycle: LifecycleCoroutineScope): RecyclerView.Adapter<FavoritesAdapter.FavouritesViewHolder>() {

    private var context2 = context

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
                    Toast.makeText(context2, "Usunięto pieśń z Ulubionych!", Toast.LENGTH_SHORT).show()
                    lifecycle.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }

                } else {
                    song.isFavorite = true
                    Toast.makeText(context2, "Dodano pieśń do Ulubionych!", Toast.LENGTH_SHORT).show()
                    lifecycle.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }
                }
            }

    }

    private fun updateSongInDatabase(song: SongEntity) {
        val songDao = SongbookDatabase.getInstance(context2).songDao()

        lifecycle.launch(Dispatchers.IO) {
            songDao.updateFavoriteSongs(song)
        }
    }

    fun sortAlphabetically() {
        val collator = Collator.getInstance()

        lifecycle.launch(Dispatchers.Default + coroutineExceptionHandler) {
            songEntities = songEntities.sortedBy { collator.getCollationKey(it.title) }
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }
    }

    fun sortNumerically() {
        lifecycle.launch(Dispatchers.Default + coroutineExceptionHandler) {
            songEntities = songEntities.sortedBy { it.number }
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }
    }

}