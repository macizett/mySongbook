package com.mayonnaise.mysongbook

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.imageview.ShapeableImageView
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.slider.Slider


class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val songbookduchowe: ShapeableImageView = findViewById(R.id.songbookduchowe)
        val songbookwedrowiec: ShapeableImageView = findViewById(R.id.songbookwedrowiec)
        val songbookbialy: ShapeableImageView = findViewById(R.id.songbookbialy)
        var infoButton: Button = findViewById(R.id.buttonInfo)
        var musicModeSwitch: Switch = findViewById(R.id.switchMusicMode)

        var Color = Color.WHITE

        var musicSwitch: Boolean = false

        val songslistopen = Intent(this, SongsList::class.java)

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_dark)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_dark)
                songbookbialy.setImageResource(R.drawable.bialy_dark)
                Color = android.graphics.Color.BLACK
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_light)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_light)
                songbookbialy.setImageResource(R.drawable.bialy_light)
                Color = android.graphics.Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_light)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_light)
                songbookbialy.setImageResource(R.drawable.bialy_light)
                Color = android.graphics.Color.WHITE
            }
        }

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
            Context.MODE_PRIVATE)
        }

        if(sharedPrefs.getBoolean("musicSwitchStatement", false) == true){
            musicSwitch = true
            musicModeSwitch.setChecked(true)
            chosenSongStore.musicMode = true

        }
        else{
            musicSwitch = false
            musicModeSwitch.setChecked(false)
            chosenSongStore.musicMode = false
        }


        musicModeSwitch.setOnClickListener{
            if(musicModeSwitch.isChecked == true){
                musicSwitch = true
                sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                chosenSongStore.musicMode = true
            }
            else{
                musicSwitch = false
                sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                chosenSongStore.musicMode = false
            }

        }


        songbookduchowe.setOnClickListener{
            chosenSongStore.chosenSongbook = "duchowe"
            chosenSongStore.maxSongNumber = 285
            startActivity(songslistopen)
        }

        songbookwedrowiec.setOnClickListener{
            chosenSongStore.chosenSongbook = "wedrowiec"
            chosenSongStore.maxSongNumber = 299
            startActivity(songslistopen)
        }

        songbookbialy.setOnClickListener{
            chosenSongStore.chosenSongbook = "bialy"
            chosenSongStore.maxSongNumber = 146
            startActivity(songslistopen)
        }

        infoButton.setOnClickListener{
                val infoDialog = Dialog(this)
                infoDialog.setContentView(R.layout.music_info_dialog)
                infoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color))
                infoDialog.show()
        }



    }
}