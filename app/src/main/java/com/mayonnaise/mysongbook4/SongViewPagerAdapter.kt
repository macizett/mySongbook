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
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.mayonnaise.mysongbook4.databinding.FavoritesViewRowBinding
import com.mayonnaise.mysongbook4.databinding.SongItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongViewPagerAdapter(private val songEntities: List<SongEntity>, var lifecycle: LifecycleCoroutineScope,
                           var context: Context,
                           private var isSearchPhraseActivity: Boolean,
                           private var phrase: String
) : RecyclerView.Adapter<SongViewPagerAdapter.CarouselViewHolder>(){

    class CarouselViewHolder(binding: SongItemBinding): RecyclerView.ViewHolder(binding.root) {
        val songTV = binding.songTV
        val buttonAddToFav = binding.buttonAddToFav
        val scrollView = binding.scrollView
        val numberAndTitleTV = binding.numberAndTitleTV
        val buttonRef = binding.buttonRef
        val refButtonLayout = binding.refButtonLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songViewPagerBinding = SongItemBinding.inflate(inflater, parent, false)
        return CarouselViewHolder(songViewPagerBinding)
        }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {

        val song = songEntities[position]
        val songTV = holder.songTV
        val buttonRef = holder.buttonRef
        val buttonAddToFav: CheckBox = holder.buttonAddToFav
        val numberAndTitleTV: TextView = holder.numberAndTitleTV
        val refButtonLayout: FrameLayout = holder.refButtonLayout

        songTV.setTypeface(null, DataManager.textStyle)
        songTV.textAlignment = DataManager.textAlignment

        numberAndTitleTV.setTypeface(null, DataManager.textStyle)

        songTV.textSize = DataManager.textSize+1
        numberAndTitleTV.textSize = DataManager.textSize+1

        if(isSearchPhraseActivity){
            numberAndTitleTV.setBackgroundResource(R.drawable.item_clickable_background)
            numberAndTitleTV.setOnClickListener{
                DataManager.chosenSong = song.number
                if(DataManager.musicMode){
                    val showSongViewMusicMode = Intent(context, SongViewMusicMode::class.java)
                    holder.itemView.context.startActivity(showSongViewMusicMode)
                }
                else{
                    val showSongView = Intent(context, SongView::class.java)
                    holder.itemView.context.startActivity(showSongView)
                }
            }
        }

        buttonAddToFav.isChecked = song.isFavorite


        buttonAddToFav.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                lifecycle.launch(Dispatchers.IO) {
                    song.isFavorite = true
                    updateSongInDatabase(song)
                }
            } else {
                lifecycle.launch(Dispatchers.IO) {
                    song.isFavorite = false
                    updateSongInDatabase(song)
                }
            }
        }

        fun isWordBoundary(text: String, index: Int): Boolean {
            return index == 0 || index == text.length || !Character.isLetterOrDigit(text[index - 1]) || !Character.isLetterOrDigit(text[index])
        }

        fun scrollScrollViewToHighlightedWord(spannable: SpannableStringBuilder) {
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


        val targetSubstring = "Ref" // Substring to scroll to
        val fullText = song.text// Get the full text from resources

        val startIndex = fullText.indexOf(targetSubstring)

        if (startIndex != -1) {
            refButtonLayout.visibility = View.VISIBLE
        }
        else{
            refButtonLayout.visibility = View.INVISIBLE
        }

        buttonRef.setOnClickListener {
            holder.songTV.post {
                holder.scrollView.smoothScrollTo(0, holder.songTV.layout.getLineTop(holder.songTV.layout.getLineForOffset(startIndex)))
            }
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
                            ForegroundColorSpan(ContextCompat.getColor(holder.itemView.context, R.color.foundphrase)),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    startIndex = song.text.indexOf(word, startIndex + word.length, true)
                }
            }

            scrollScrollViewToHighlightedWord(spannable)
        }

        else{
            songTV.text = songEntities[position].text
        }

        numberAndTitleTV.text = "${song.number}. ${song.title}"
    }

    override fun getItemCount(): Int = songEntities.size


    fun updateSongInDatabase(song: SongEntity) {
        val songDao = SongbookDatabase.getInstance(context).songDao()

        lifecycle.launch(Dispatchers.IO) {
            songDao.updateFavoriteSongs(song)
        }
    }

    }