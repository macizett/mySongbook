package com.mayonnaise.mysongbook4

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarouselAdapter(private val carouselDataList: ArrayList<Int>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselItemViewHolder>() {

    class CarouselItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselItemViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.songbook_item, parent, false)
        return CarouselItemViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: CarouselItemViewHolder, position: Int) {
        val songDao = SongbookDatabase.getInstance(holder.itemView.context).songDao()
        val ImageView = holder.itemView.findViewById<ShapeableImageView>(R.id.songbookImage)
        ImageView.setImageResource(carouselDataList[position])

        holder.itemView.setOnClickListener{
            val songslistopen = Intent(holder.itemView.context, SongsList::class.java)
            DataManager.chosenSongbook = position+1
            GlobalScope.launch(Dispatchers.IO) {
                DataManager.maxSongNumber = songDao.getAllSongsBySongbook(position+1).size
                withContext(Dispatchers.Main){
                    holder.itemView.context.startActivity(songslistopen)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return carouselDataList.size
    }

}