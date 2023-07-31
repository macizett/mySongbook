package com.mayonnaise.mysongbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mayonnaise.mysongbook.databinding.TocViewRowBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class SongAdapter(private var songEntities: List<SongEntity>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }

    inner class SongViewHolder(binding: TocViewRowBinding): ViewHolder(binding.root){
        val titleTV = binding.titleTV
        val numberTV = binding.numberAndTitleTV

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val tocViewRowBinding = TocViewRowBinding.inflate(inflater, parent, false)
        return SongViewHolder(tocViewRowBinding)
    }

    override fun getItemCount(): Int {
        return songEntities.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.titleTV.text = songEntities[position].title
        holder.numberTV.text = songEntities[position].number.toString()

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
}

    fun sortAlphabetically() {
        val collator = Collator.getInstance()
        GlobalScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            songEntities = songEntities.sortedBy {
                collator.getCollationKey(it.title)
            }
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }
    }

    fun sortNumerically() {
        GlobalScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            songEntities = songEntities.sortedBy { it.number }
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }

    }
}