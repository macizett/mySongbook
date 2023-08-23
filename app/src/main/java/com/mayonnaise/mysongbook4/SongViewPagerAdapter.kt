package com.mayonnaise.mysongbook4

import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SongViewPagerAdapter(private val songEntities: List<SongEntity>, context: Context) : RecyclerView.Adapter<SongViewPagerAdapter.CarouselViewHolder>(){

    var context2 = context
    var isFavorite = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
            return CarouselViewHolder(view)
        }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val song = songEntities[position]
        val songTV = holder.songTV
        val buttonAddToFav: CheckBox = holder.buttonAddToFav
        val numberAndTitleTV: TextView = holder.numberAndTitleTV
        songTV.text = songEntities[position].text

        songTV.textSize = DataManager.textSize+1
        numberAndTitleTV.textSize = DataManager.textSize+1

        buttonAddToFav.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                GlobalScope.launch(Dispatchers.IO) {
                    song.isFavorite = true
                    updateSongInDatabase(song)
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    song.isFavorite = false
                    updateSongInDatabase(song)
                }
            }
        }

        numberAndTitleTV.text = "${song.number}. ${song.title}"
        if(song.isFavorite){
            buttonAddToFav.isChecked = true
        }
        else{
            buttonAddToFav.isChecked = false
        }

        val gestureDetector = GestureDetectorCompat(songTV.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (song.isFavorite) {
                    song.isFavorite = false
                    buttonAddToFav.isChecked = false
                    Toast.makeText(context2, "Usunięto pieśń z Ulubionych!", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }

                } else {
                    song.isFavorite = true
                    buttonAddToFav.isChecked = true
                    Toast.makeText(context2, "Dodano pieśń do Ulubionych!", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }
                }
                return true
            }
        })

        songTV.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun getItemCount(): Int = songEntities.size

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songTV: TextView = itemView.findViewById(R.id.songTV)
        val buttonAddToFav: CheckBox = itemView.findViewById(R.id.buttonAddToFav)
        val numberAndTitleTV: TextView = itemView.findViewById(R.id.numberAndTitleTV)
    }

    fun updateSongInDatabase(song: SongEntity) {
        val songDao = SongbookDatabase.getInstance(context2).songDao()

        GlobalScope.launch(Dispatchers.IO) {
            songDao.updateFavoriteSongs(song)
        }
    }

    }