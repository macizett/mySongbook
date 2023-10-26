package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongbookViewPagerAdapter(private val carouselDataList: ArrayList<Int>, var lifecycle: LifecycleCoroutineScope, var context: Context) :
    RecyclerView.Adapter<SongbookViewPagerAdapter.CarouselItemViewHolder>() {

    class CarouselItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselItemViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.songbook_item, parent, false)
        return CarouselItemViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: CarouselItemViewHolder, position: Int) {
        val songDao = SongbookDatabase.getInstance(holder.itemView.context).songDao()
        val imageView = holder.itemView.findViewById<ShapeableImageView>(R.id.songbookImage)

        imageView.setImageResource(carouselDataList[position])

        holder.itemView.setOnClickListener{
            val songsListOpen = Intent(holder.itemView.context, SongsList::class.java)
            DataManager.chosenSongbook = position+2

            DataManager.chosenSongbookName = when (DataManager.chosenSongbook){
                2 -> "Śpiewnik Wędrowiec"
                3 -> "Śpiewnik Młodzieżowy"
                else -> "Nieznany śpiewnik"
            }

            lifecycle.launch(Dispatchers.IO) {
                DataManager.maxSongNumber = songDao.getAllSongsBySongbook(position+2).size
                withContext(Dispatchers.Main){
                        holder.itemView.context.startActivity(songsListOpen)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return carouselDataList.size
    }

}