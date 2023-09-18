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
import com.mayonnaise.mysongbook4.databinding.TocViewRowBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator

class SongListAdapter(private var songEntities: List<SongEntity>, private var lifecycleScope: LifecycleCoroutineScope,
                      private var recyclerView: RecyclerView, private var progressBar: ProgressBar,
                      private var context: Context) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }

    inner class SongViewHolder(binding: TocViewRowBinding): ViewHolder(binding.root){
        val titleTV = binding.titleTV
        val numberTV = binding.numberTV

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

        holder.titleTV.textSize = DataManager.textSize-3
        holder.numberTV.textSize = DataManager.textSize-3

        holder.titleTV.setTypeface(null, DataManager.textStyle)

        holder.itemView.setOnClickListener{
            DataManager.chosenSong = songEntities[position].number
            goToNumber(DataManager.chosenSong)
        }
}
    fun goToNumber(number: Int){
        DataManager.chosenSong = number
        if(DataManager.musicMode){
            val showSongViewMusicMode = Intent(context, SongViewMusicMode::class.java)
            context.startActivity(showSongViewMusicMode)
        }
        else{
            val showSongView = Intent(context, SongView::class.java)
            context.startActivity(showSongView)
        }
    }

    fun sort(sortingPreference: Boolean) {
        val collator = Collator.getInstance()
        progressBar.visibility = View.VISIBLE
        if(!sortingPreference){
            lifecycleScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
                songEntities = songEntities.sortedBy {
                    collator.getCollationKey(it.title)
                }
                withContext(Dispatchers.Main){
                    recyclerView.animate()
                        .alpha(0f)
                        .setDuration(200L)
                        .withEndAction {
                            notifyDataSetChanged()
                            progressBar.visibility = View.GONE
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
                songEntities = songEntities.sortedBy { it.number }
                delay(200L)
                withContext(Dispatchers.Main){
                    recyclerView.animate()
                        .alpha(0f)
                        .setDuration(200L)
                        .withEndAction {
                            notifyDataSetChanged()
                            progressBar.visibility = View.INVISIBLE
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