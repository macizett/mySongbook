package com.mayonnaise.mysongbook4

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongViewPagerAdapter(private val songEntities: List<SongEntity>, context: Context, isSearchPhraseActivity: Boolean, phrase: String) : RecyclerView.Adapter<SongViewPagerAdapter.CarouselViewHolder>(){

    var context = context
    var isSearchPhraseActivity = isSearchPhraseActivity
    var phrase = phrase

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
            return CarouselViewHolder(view)
        }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val song = songEntities[position]
        val songTV = holder.songTV
        val buttonAddToFav: CheckBox = holder.buttonAddToFav
        val numberAndTitleTV: TextView = holder.numberAndTitleTV

        songTV.textSize = DataManager.textSize+1
        numberAndTitleTV.textSize = DataManager.textSize+1

        if(isSearchPhraseActivity){
            numberAndTitleTV.setOnClickListener{
                DataManager.chosenSong = song.number
                if(DataManager.musicMode == true){
                    val showSongViewMusicMode = Intent(context, SongViewMusicMode::class.java)
                    holder.itemView.context.startActivity(showSongViewMusicMode)
                }
                else{
                    val showSongView = Intent(context, SongView::class.java)
                    holder.itemView.context.startActivity(showSongView)
                }
            }
        }

        if(song.isFavorite){
            buttonAddToFav.isChecked = true
        }
        else{
            buttonAddToFav.isChecked = false
        }


        buttonAddToFav.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonAddToFav.isChecked) {
                GlobalScope.launch(Dispatchers.IO) {
                    song.isFavorite = true
                    updateSongInDatabase(song)
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Dodano pieśń do Ulubionych", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    song.isFavorite = false
                    updateSongInDatabase(song)
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Usunięto pieśń z Ulubionych", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val gestureDetector = GestureDetectorCompat(songTV.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (song.isFavorite) {
                    song.isFavorite = false
                    buttonAddToFav.isChecked = false
                    Toast.makeText(context, "Usunięto pieśń z Ulubionych!", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch(Dispatchers.IO) {
                        updateSongInDatabase(song)
                    }

                } else {
                    song.isFavorite = true
                    buttonAddToFav.isChecked = true
                    Toast.makeText(context, "Dodano pieśń do Ulubionych!", Toast.LENGTH_SHORT).show()
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

        fun isWordBoundary(text: String, index: Int): Boolean {
            return index == 0 || index == text.length || !Character.isLetterOrDigit(text[index - 1]) || !Character.isLetterOrDigit(text[index])
        }

        fun scrollScrollViewToHighlightedWord(spannable: SpannableStringBuilder, originalText: String) {
            val highlightSpans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
            if (highlightSpans.isNotEmpty()) {
                val firstHighlightedSpan = highlightSpans[0]
                val startIndex = spannable.getSpanStart(firstHighlightedSpan)

                songTV.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        songTV.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val layout = songTV.layout
                        val line = layout.getLineForOffset(startIndex)
                        val y = layout.getLineTop(line)

                        holder.scrollView.smoothScrollTo(0, y)
                    }
                })
            }

            songTV.text = spannable
        }


        if(phrase.isNotEmpty()) {

            val wordsToHighlight = phrase.split(" ")
            val spannable = SpannableStringBuilder(song.text)

            for (word in wordsToHighlight) {
                var startIndex = song.text.indexOf(word, 0, true)

                while (startIndex != -1) {

                    val endIndex = startIndex + word.length
                    if (isWordBoundary(song.text, startIndex) && isWordBoundary(
                            song.text,
                            endIndex
                        )
                    ) {

                        spannable.setSpan(
                            ForegroundColorSpan(holder.itemView.context.resources.getColor(R.color.foundphrase)),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    startIndex = song.text.indexOf(word, startIndex + word.length, true)
                }
            }

            scrollScrollViewToHighlightedWord(spannable, song.text)
        }

        else{
            songTV.text = songEntities[position].text
        }

        numberAndTitleTV.text = "${song.number}. ${song.title}"
    }

    override fun getItemCount(): Int = songEntities.size

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songTV: TextView = itemView.findViewById(R.id.songTV)
        val buttonAddToFav: CheckBox = itemView.findViewById(R.id.buttonAddToFav)
        val numberAndTitleTV: TextView = itemView.findViewById(R.id.numberAndTitleTV)
        val scrollView: ScrollView = itemView.findViewById(R.id.scrollView)
    }

    fun updateSongInDatabase(song: SongEntity) {
        val songDao = SongbookDatabase.getInstance(context).songDao()

        GlobalScope.launch(Dispatchers.IO) {
            songDao.updateFavoriteSongs(song)
        }
    }

    }