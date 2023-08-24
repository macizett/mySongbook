package com.mayonnaise.mysongbook4

import android.app.Dialog
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val reportButton: Button = findViewById(R.id.buttonReport)
        val settingsButton: Button = findViewById(R.id.buttonSettings)
        val initializeTV: TextView = findViewById(R.id.initializeTV)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val contextView: ConstraintLayout = findViewById(R.id.constraintlayout)
        val verseTV: TextView = findViewById(R.id.verseTV)
        val versePlaceTV: TextView = findViewById(R.id.verse_place_TV)
        val msbTV: TextView = findViewById(R.id.textViewMSB)
        val lineLayout: LinearLayout = findViewById(R.id.lineLayout)
        val lineLayout2: LinearLayout = findViewById(R.id.lineLayout2)

        val reportEmail = "mysongbook.report@gmail.com"

        var data = arrayListOf<Int>()

        val PREF_INITIALIZED_KEY = "pref_initialized_key"
        var isInitialized = false

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        val nightMode = sharedPrefs.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        AppCompatDelegate.setDefaultNightMode(nightMode)

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                data = arrayListOf(R.drawable.duchowe_light, R.drawable.wedrowiec_light, R.drawable.bialy_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                data = arrayListOf(R.drawable.duchowe_dark, R.drawable.wedrowiec_dark, R.drawable.bialy_dark)
            }
        }

        var verseDao = SongbookDatabase.getInstance(this).verseDao()


        var textSize = sharedPrefs.getFloat("textSize", 20.0F)
        DataManager.textSize = textSize
        verseTV.textSize = DataManager.textSize
        versePlaceTV.textSize = DataManager.textSize
        reportButton.textSize = DataManager.textSize-5
        settingsButton.textSize = DataManager.textSize-5
        msbTV.textSize = DataManager.textSize-8


        viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).overScrollMode =
                RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((20 * Resources.getSystem().displayMetrics.density).toInt()))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.70f + r * 0.30f)
        }
        viewPager.setPageTransformer(compositePageTransformer)

        viewPager.adapter = CarouselAdapter(data)




        fun randomVerse(){
            lifecycleScope.launch(Dispatchers.IO){
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

                    lineLayout.visibility = View.VISIBLE
                    lineLayout.alpha = 0f
                    lineLayout.animate().alpha(1f).start()

                    lineLayout2.visibility = View.VISIBLE
                    lineLayout2.alpha = 0f
                    lineLayout2.animate().alpha(1f).start()

                    msbTV.visibility = View.VISIBLE
                    msbTV.alpha = 0f
                    msbTV.animate().alpha(1f).start()

                    verseTV.visibility = View.VISIBLE
                    verseTV.alpha = 0f
                    verseTV.animate().alpha(1f).start()

                    versePlaceTV.visibility = View.VISIBLE
                    versePlaceTV.alpha = 0f
                    versePlaceTV.animate().alpha(1f).start()

                    viewPager.visibility = View.VISIBLE
                    viewPager.alpha = 0f
                    viewPager.animate().alpha(1f).start()
                }
            }
        }

        suspend fun initializeSongsIfNeeded(context: Context) {
            if (!isInitialized) {

                if (!sharedPrefs.getBoolean(PREF_INITIALIZED_KEY, false)) {
                    progressBar.setVisibility(View.VISIBLE)
                    initializeTV.setVisibility(View.VISIBLE)

                    withContext(Dispatchers.Default) {
                        SongParser.initialize(context, false)
                    }

                    withContext(Dispatchers.Main) {
                        initializeTV.setVisibility(View.GONE)
                        progressBar.setVisibility(View.GONE)
                    }
                    sharedPrefs.edit().putBoolean(PREF_INITIALIZED_KEY, true).apply()
                }

                isInitialized = true
            }
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

        settingsButton.setOnClickListener {
            val settingsDialog = Dialog(this)
            settingsDialog.setContentView(R.layout.settings_dialog)
            val sliderTextSize: Slider = settingsDialog.findViewById(R.id.sliderTextSize)
            val nightModeSwitch: IconSwitch = settingsDialog.findViewById(R.id.nightModeSwitch)
            val textView2: TextView = settingsDialog.findViewById(R.id.textView2)
            val textView3: TextView = settingsDialog.findViewById(R.id.textView3)

            sliderTextSize.value = textSize

            textView2.textSize = textSize-2
            textView3.textSize = textSize-2

            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    nightModeSwitch.setChecked(IconSwitch.Checked.RIGHT)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    nightModeSwitch.setChecked(IconSwitch.Checked.LEFT)
                }
            }

            nightModeSwitch.setCheckedChangeListener(object : IconSwitch.CheckedChangeListener {
                override fun onCheckChanged(current: IconSwitch.Checked?) {
                    var newNightMode = AppCompatDelegate.MODE_NIGHT_NO

                    when (current) {
                        IconSwitch.Checked.LEFT -> {
                            newNightMode = AppCompatDelegate.MODE_NIGHT_NO
                        }

                        IconSwitch.Checked.RIGHT -> {
                            newNightMode = AppCompatDelegate.MODE_NIGHT_YES
                        }
                        else -> Toast.makeText(this@MainActivity, "ERROR 2137", Toast.LENGTH_SHORT).show()
                    }

                    sharedPrefs.edit().putInt("nightMode", newNightMode).apply()

                    Handler(Looper.getMainLooper()).postDelayed({
                        settingsDialog.hide()
                        AppCompatDelegate.setDefaultNightMode(newNightMode)
                    }, 1_000)


                }
            })

            sliderTextSize.addOnChangeListener(object : Slider.OnChangeListener {
                override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                    textView2.textSize = value-2
                    textView3.textSize = value-2
                    verseTV.textSize = value
                    versePlaceTV.textSize = value-4
                    reportButton.textSize = value-5
                    settingsButton.textSize = value-5
                    msbTV.textSize = value-8

                    DataManager.textSize = value
                    textSize = value
                    sharedPrefs.edit().putFloat("textSize", sliderTextSize.value).apply()
                }
            })
            settingsDialog.show()
        }

        lifecycleScope.launch(Dispatchers.IO){
            initializeSongsIfNeeded(this@MainActivity)
        }

        if (sharedPrefs.getBoolean("databaseStatement", false)){
                randomVerse()
        }

        else{
            randomVerse()
            verseTV.text = "Witamy w MySongbook! Zapraszamy do zapoznania się z wszystkimi funkcjami naszej aplikacji :)"
            sharedPrefs.edit().putBoolean("databaseStatement", true).apply()
        }

    }
}