package com.mayonnaise.mysongbook

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val songbookRV: RecyclerView = findViewById(R.id.carousel_recycler_view)
        val infoButton: Button = findViewById(R.id.buttonInfo)
        val reportButton: Button = findViewById(R.id.buttonReport)
        val reportTV: TextView = findViewById(R.id.reportTV)
        val infoTV: TextView = findViewById(R.id.infoTV)
        val chooseTV: TextView = findViewById(R.id.chooseTV)
        val initializeTV: TextView = findViewById(R.id.initializeTV)
        val musicModeSwitch: Switch = findViewById(R.id.switchMusicMode)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val contextView: ConstraintLayout = findViewById(R.id.constraintlayout)
        val verseTV: TextView = findViewById(R.id.verseTV)
        val versePlaceTV: TextView = findViewById(R.id.verse_place_TV)
        val lineLayout: LinearLayout = findViewById(R.id.lineLayout)
        val lineLayout2: LinearLayout = findViewById(R.id.lineLayout2)
        val lineLayout3: LinearLayout = findViewById(R.id.lineLayout3)

        val reportEmail = "mysongbook.report@gmail.com"

        var musicSwitch: Boolean = false

        var data = arrayListOf<Int>()

        var verseDao = SongbookDatabase.getInstance(this).verseDao()

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                data = arrayListOf(R.drawable.duchowe_light, R.drawable.wedrowiec_light, R.drawable.bialy_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                data = arrayListOf(R.drawable.duchowe_light, R.drawable.wedrowiec_light, R.drawable.bialy_light)
            }
        }
        songbookRV.adapter = CarouselAdapter(data)


        val PREF_INITIALIZED_KEY = "pref_initialized_key"
        var isInitialized = false

        fun randomVerse(){
            lineLayout.animate().alpha(0f).withEndAction { lineLayout.visibility = View.INVISIBLE }.start()
            lineLayout2.animate().alpha(0f).withEndAction { lineLayout.visibility = View.INVISIBLE }.start()

            GlobalScope.launch(Dispatchers.IO){
                var verseID = Random.nextInt(1, 39)
                var verse = verseDao.getVerse(verseID)

                withContext(Dispatchers.Main){
                    versePlaceTV.animate().alpha(0f).withEndAction {
                        versePlaceTV.text = verse.place
                        versePlaceTV.animate().alpha(1f).start()
                    }

                    verseTV.animate().alpha(0f).withEndAction {
                        verseTV.text = verse.text
                        verseTV.animate().alpha(1f).start()
                    }

                    delay(400)

                    lineLayout.visibility = View.VISIBLE
                    lineLayout.alpha = 0f
                    lineLayout.animate().alpha(1f).start()

                    lineLayout2.visibility = View.VISIBLE
                    lineLayout2.alpha = 0f
                    lineLayout2.animate().alpha(1f).start()
                }
            }
        }

        suspend fun initializeSongsIfNeeded(context: Context) {
            if (!isInitialized) {
                val sharedPrefs = context.getSharedPreferences("your_shared_prefs_name", Context.MODE_PRIVATE)

                if (!sharedPrefs.getBoolean(PREF_INITIALIZED_KEY, false)) {
                    progressBar.setVisibility(View.VISIBLE)
                    initializeTV.setVisibility(View.VISIBLE)
                    infoTV.setVisibility(View.GONE)
                    chooseTV.setVisibility(View.GONE)
                    songbookRV.visibility = View.GONE
                    infoButton.setVisibility(View.GONE)
                    musicModeSwitch.setVisibility(View.GONE)
                    reportButton.visibility = View.GONE
                    reportTV.visibility = View.GONE
                    verseTV.visibility = View.GONE
                    versePlaceTV.visibility = View.GONE
                    lineLayout.visibility = View.GONE
                    lineLayout2.visibility = View.GONE
                    lineLayout3.visibility = View.GONE

                    withContext(Dispatchers.Default) {
                        SongParser.initialize(context, false)
                        delay(1000)
                    }

                    withContext(Dispatchers.Main) {
                        initializeTV.setVisibility(View.GONE)
                        progressBar.setVisibility(View.GONE)
                        infoTV.setVisibility(View.VISIBLE)
                        chooseTV.setVisibility(View.VISIBLE)
                        songbookRV.visibility = View.VISIBLE
                        infoButton.setVisibility(View.VISIBLE)
                        musicModeSwitch.setVisibility(View.VISIBLE)
                        reportButton.visibility = View.VISIBLE
                        reportTV.visibility = View.VISIBLE
                        verseTV.visibility = View.VISIBLE
                        versePlaceTV.visibility = View.VISIBLE
                        lineLayout.visibility = View.VISIBLE
                        lineLayout2.visibility = View.VISIBLE
                        lineLayout3.visibility = View.VISIBLE
                    }
                    sharedPrefs.edit().putBoolean(PREF_INITIALIZED_KEY, true).apply()
                }

                isInitialized = true
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

        infoButton.setOnClickListener{
                val infoDialog = Dialog(this)
                infoDialog.setContentView(R.layout.music_info_dialog)
                infoDialog.show()
        }

        reportButton.setOnClickListener{

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

        verseTV.setOnClickListener {
            randomVerse()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            randomVerse()
        }, 500L)

    }
}