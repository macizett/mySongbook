package com.mayonnaise.mysongbook4

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class SongViewPagerAdapter(private val contentList: List<SongEntity>, context: Context) : RecyclerView.Adapter<SongViewPagerAdapter.CarouselViewHolder>(){

    var context2 = context
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private val DOUBLE_CLICK_INTERVAL = 300
    private var lastClickTime: Long = 0
    private var scaleFactor = 1.0f


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
            return CarouselViewHolder(view)
        }

        override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
            holder.textView.text = contentList[position].text

        }

        override fun getItemCount(): Int = contentList.size

        class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.songTV)
            val scrollView: ScrollView = itemView.findViewById(R.id.scrollView)
        }
    }