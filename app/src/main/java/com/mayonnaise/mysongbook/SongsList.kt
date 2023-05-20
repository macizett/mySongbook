package com.mayonnaise.mysongbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SongsList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songslist)

        var buttonGo: Button = findViewById(R.id.buttonGo)
        var getNumber: EditText = findViewById(R.id.textInputEditText)
        var searchButton: FloatingActionButton = findViewById(R.id.searchActionButton)
        var TableOfContentsTV: TextView = findViewById(R.id.TOC_TV)
        var TableOfContentsNumberTV: TextView = findViewById(R.id.TOC_Number_TV)
        var ConstraintLayoutClick: ConstraintLayout = findViewById(R.id.constraintLayout)
        var dragButton: Button = findViewById(R.id.buttonDrag)
        val standardBottomSheet = findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)

        var maxSongNr = chosenSongStore.maxSongNumber

        if(chosenSongStore.chosenSongbook == "duchowe"){
            TableOfContentsTV.setText(R.string.duchowe_TOC_Titles)
            TableOfContentsNumberTV.setText(R.string.duchowe_TOC_Number)
        }
        if(chosenSongStore.chosenSongbook == "wedrowiec"){
            TableOfContentsTV.setText(R.string.wedrowiec_TOC)
            TableOfContentsNumberTV.setVisibility(View.INVISIBLE)
        }
        if(chosenSongStore.chosenSongbook == "bialy"){
            TableOfContentsTV.setText(R.string.bialy_TOC)
            TableOfContentsNumberTV.setVisibility(View.INVISIBLE)
        }


        buttonGo.setOnClickListener{
            if (getNumber.text.isEmpty()){
                Toast.makeText(this, "Wpisz numer pieśni!", Toast.LENGTH_SHORT).show()
            }
            else{
                var numberoo = getNumber.text.toString()
                if(numberoo.toInt() <= maxSongNr && numberoo.toInt() > 0) {
                    var number = getNumber.text.toString()
                    chosenSongStore.chosenSong = number

                        if(chosenSongStore.musicMode == true){
                            val showSongViewMusicMode = Intent(this, SongViewMusicMode::class.java)
                            startActivity(showSongViewMusicMode)
                        }
                    else{
                            val showSongView = Intent(this, SongView::class.java)
                            startActivity(showSongView)
                        }
                }
                else{
                    Toast.makeText(this, "Nieprawidłowy numer pieśni!", Toast.LENGTH_SHORT).show()
                }
            }

        }
        searchButton.setOnClickListener {
            val searchActivity = Intent(this, SearchPhrase::class.java)
            startActivity(searchActivity)
            }

        ConstraintLayoutClick.setOnClickListener{
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        dragButton.setOnClickListener{
            if(standardBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            else{
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        standardBottomSheet.setOnClickListener{
            if(standardBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            else{
                standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        }
    }
