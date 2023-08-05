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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val songbookduchowe: ShapeableImageView = findViewById(R.id.songbookduchowe)
        val songbookwedrowiec: ShapeableImageView = findViewById(R.id.songbookwedrowiec)
        val songbookbialy: ShapeableImageView = findViewById(R.id.songbookbialy)
        val infoButton: Button = findViewById(R.id.buttonInfo)
        val reportButton: Button = findViewById(R.id.buttonReport)
        val reportTV: TextView = findViewById(R.id.reportTV)
        val infoTV: TextView = findViewById(R.id.infoTV)
        val chooseTV: TextView = findViewById(R.id.chooseTV)
        val initializeTV: TextView = findViewById(R.id.initializeTV)
        val musicModeSwitch: Switch = findViewById(R.id.switchMusicMode)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val contextView: ConstraintLayout = findViewById(R.id.constraintlayout)

        val songDao = SongbookDatabase.getInstance(this).songDao()

        val reportEmail = "mysongbook.report@gmail.com"

        var musicSwitch: Boolean = false

        val songslistopen = Intent(this, SongsList::class.java)


        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_dark)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_dark)
                songbookbialy.setImageResource(R.drawable.bialy_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_light)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_light)
                songbookbialy.setImageResource(R.drawable.bialy_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                songbookduchowe.setImageResource(R.drawable.duchowe_light)
                songbookwedrowiec.setImageResource(R.drawable.wedrowiec_light)
                songbookbialy.setImageResource(R.drawable.bialy_light)
            }
        }

        val PREF_INITIALIZED_KEY = "pref_initialized_key"
        var isInitialized = false


        suspend fun initializeSongsIfNeeded(context: Context) {
            if (!isInitialized) {
                val sharedPrefs = context.getSharedPreferences("your_shared_prefs_name", Context.MODE_PRIVATE)

                if (!sharedPrefs.getBoolean(PREF_INITIALIZED_KEY, false)) {
                    progressBar.setVisibility(View.VISIBLE)
                    initializeTV.setVisibility(View.VISIBLE)
                    infoTV.setVisibility(View.GONE)
                    chooseTV.setVisibility(View.GONE)
                    songbookduchowe.setVisibility(View.GONE)
                    songbookwedrowiec.setVisibility(View.GONE)
                    songbookbialy.setVisibility(View.GONE)
                    infoButton.setVisibility(View.GONE)
                    musicModeSwitch.setVisibility(View.GONE)
                    reportButton.visibility = View.GONE
                    reportTV.visibility = View.GONE

                    withContext(Dispatchers.Default) {
                        SongParser.initialize(context, false)
                    }

                    withContext(Dispatchers.Main) {
                        initializeTV.setVisibility(View.GONE)
                        infoTV.setVisibility(View.VISIBLE)
                        chooseTV.setVisibility(View.VISIBLE)
                        progressBar.setVisibility(View.GONE)
                        songbookduchowe.setVisibility(View.VISIBLE)
                        songbookwedrowiec.setVisibility(View.VISIBLE)
                        songbookbialy.setVisibility(View.VISIBLE)
                        infoButton.setVisibility(View.VISIBLE)
                        musicModeSwitch.setVisibility(View.VISIBLE)
                        reportButton.visibility = View.VISIBLE
                        reportTV.visibility = View.VISIBLE
                    }
                    sharedPrefs.edit().putBoolean(PREF_INITIALIZED_KEY, true).apply()
                }

                isInitialized = true
            }
        }

        fun openSongbook(songbook: Int){
            DataManager.chosenSongbook = songbook
            GlobalScope.launch(Dispatchers.IO) {
                DataManager.maxSongNumber = songDao.getAllSongsBySongbook(songbook).size
                withContext(Dispatchers.Main){
                    startActivity(songslistopen)
                }
            }
        }

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
            Context.MODE_PRIVATE)
        }

        if(sharedPrefs.getBoolean("musicSwitchStatement", false)){
            musicSwitch = true
            musicModeSwitch.setChecked(true)
            DataManager.musicMode = true

        }
        else{
            musicSwitch = false
            musicModeSwitch.setChecked(false)
            DataManager.musicMode = false
        }


        musicModeSwitch.setOnClickListener{
            if(musicModeSwitch.isChecked == true){
                musicSwitch = true
                sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                DataManager.musicMode = true
            }
            else{
                musicSwitch = false
                sharedPrefs.edit().putBoolean("musicSwitchStatement", musicSwitch).apply()
                DataManager.musicMode = false
            }

        }


        songbookduchowe.setOnClickListener{
            openSongbook(1)
        }

        songbookwedrowiec.setOnClickListener{
            openSongbook(2)
        }

        songbookbialy.setOnClickListener{
            openSongbook(3)
        }

        infoButton.setOnClickListener{
                val infoDialog = Dialog(this)
                infoDialog.setContentView(R.layout.music_info_dialog)
                infoDialog.show()
        }

        reportButton.setOnClickListener{

            //Toast.makeText(this, "Użyj twojej aplikacji do obsługi Email", Toast.LENGTH_LONG).show()
            var snackbar = Snackbar.make(contextView, "Użyj twojej aplikacji do obsługi Email", Snackbar.LENGTH_LONG)

                snackbar.setAction("OK") {
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(reportEmail))
                    }

                    if (emailIntent.resolveActivity(packageManager) != null) {
                        startActivity(emailIntent)
                    } else {
                        Toast.makeText(this, "Nie znaleziono aplikacji do obsługi Email", Toast.LENGTH_LONG).show()
                    }
                }.show()

            snackbar.setActionTextColor(Color.RED)

        }

        GlobalScope.launch(Dispatchers.IO){
            initializeSongsIfNeeded(this@MainActivity)
        }
    }
}