package com.mayonnaise.mysongbook4

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.mayonnaise.mysongbook4.databinding.ActivityMainBinding
import com.mayonnaise.mysongbook4.databinding.SettingsDialogBinding
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val reportEmail = "mysongbook.report@gmail.com"

        var data = arrayListOf<Int>()

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

        val verseDao = SongbookDatabase.getInstance(this).verseDao()


        var textSize = sharedPrefs.getFloat("textSize", 20.0F)
        DataManager.textSize = textSize
        binding.verseTV.textSize = DataManager.textSize
        binding.versePlaceTV.textSize = DataManager.textSize-5
        binding.buttonReport.textSize = DataManager.textSize-5
        binding.buttonSettings.textSize = DataManager.textSize-5
        binding.textViewMSB.textSize = DataManager.textSize-8


        binding.viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).overScrollMode =
                RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((20 * Resources.getSystem().displayMetrics.density).toInt()))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = (0.70f + r * 0.30f)
        }
        binding.viewPager.setPageTransformer(compositePageTransformer)

        binding.viewPager.adapter = SongbookViewPagerAdapter(data, lifecycleScope)




        fun randomVerse(){
            lifecycleScope.launch(Dispatchers.IO){
                val verseID = Random.nextInt(1, 39)
                val verse = verseDao.getVerse(verseID)

                withContext(Dispatchers.Main){

                    binding.versePlaceTV.animate().alpha(0f).withEndAction {
                        binding.versePlaceTV.text = verse.place
                        binding.versePlaceTV.animate().alpha(1f).start()
                    }

                    binding.verseTV.animate().alpha(0f).withEndAction {
                        binding.verseTV.text = verse.text
                        binding.verseTV.animate().alpha(1f).start()
                    }

                    binding.lineLayout.visibility = View.VISIBLE
                    binding.lineLayout.alpha = 0f
                    binding.lineLayout.animate().alpha(1f).start()

                    binding.lineLayout2.visibility = View.VISIBLE
                    binding.lineLayout2.alpha = 0f
                    binding.lineLayout2.animate().alpha(1f).start()

                    binding.textViewMSB.visibility = View.VISIBLE
                    binding.textViewMSB.alpha = 0f
                    binding.textViewMSB.animate().alpha(1f).start()

                    binding.verseTV.visibility = View.VISIBLE
                    binding.verseTV.alpha = 0f
                    binding.verseTV.animate().alpha(1f).start()

                    binding.versePlaceTV.visibility = View.VISIBLE
                    binding.versePlaceTV.alpha = 0f
                    binding.versePlaceTV.animate().alpha(1f).start()

                    binding.viewPager.visibility = View.VISIBLE
                    binding.viewPager.alpha = 0f
                    binding.viewPager.animate().alpha(1f).start()
                }
            }
        }

        suspend fun initializeSongsIfNeeded(context: Context) {
            if (!isInitialized) {

                if (!sharedPrefs.getBoolean("pref_initialized_key", false)) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.initializeTV.visibility = View.VISIBLE

                    withContext(Dispatchers.Default) {
                        SongParser.initialize(context, false, lifecycleScope)
                    }
                    delay(1000)

                    withContext(Dispatchers.Main) {
                        binding.initializeTV.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    }
                    sharedPrefs.edit().putBoolean("pref_initialized_key", true).apply()
                }

                isInitialized = true
            }
        }


        binding.buttonReport.setOnClickListener{

            val snackbar = Snackbar.make(binding.constraintlayout, "Użyj twojej aplikacji do obsługi Email", Snackbar.LENGTH_LONG)


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

        binding.buttonSettings.setOnClickListener {
            val settingsDialog = Dialog(this)
            val dialogBinding = SettingsDialogBinding.inflate(layoutInflater)
            settingsDialog.setContentView(dialogBinding.root)

            val sliderTextSize: Slider = dialogBinding.sliderTextSize
            val nightModeSwitch: IconSwitch = dialogBinding.nightModeSwitch

            sliderTextSize.value = textSize

            dialogBinding.textView2.textSize = textSize - 2
            dialogBinding.textView3.textSize = textSize - 2


            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    nightModeSwitch.checked = IconSwitch.Checked.RIGHT
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    nightModeSwitch.checked = IconSwitch.Checked.LEFT
                }
            }

            nightModeSwitch.setCheckedChangeListener { current ->
                var newNightMode = AppCompatDelegate.MODE_NIGHT_NO

                when (current) {
                    IconSwitch.Checked.LEFT -> {
                        newNightMode = AppCompatDelegate.MODE_NIGHT_NO
                    }

                    IconSwitch.Checked.RIGHT -> {
                        newNightMode = AppCompatDelegate.MODE_NIGHT_YES
                    }

                    else -> Toast.makeText(this@MainActivity, "ERROR 2137", Toast.LENGTH_SHORT)
                        .show()
                }

                sharedPrefs.edit().putInt("nightMode", newNightMode).apply()

                Handler(Looper.getMainLooper()).postDelayed({
                    settingsDialog.hide()
                    AppCompatDelegate.setDefaultNightMode(newNightMode)
                }, 1_000)
            }

            sliderTextSize.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
                dialogBinding.textView2.textSize = value-2
                dialogBinding.textView3.textSize = value-2
                binding.verseTV.textSize = value
                binding.versePlaceTV.textSize = value-5
                binding.buttonReport.textSize = value-5
                binding.buttonSettings.textSize = value-5
                binding.textViewMSB.textSize = value-8

                DataManager.textSize = value
                textSize = value
                sharedPrefs.edit().putFloat("textSize", sliderTextSize.value).apply()
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
            binding.verseTV.text = "Witamy w MySongbook! Zapraszamy do zapoznania się z wszystkimi funkcjami naszej aplikacji :)"
            binding.viewPager.visibility = View.VISIBLE
            binding.viewPager.alpha = 0f
            binding.viewPager.animate().alpha(1f).start()

            binding.lineLayout.visibility = View.VISIBLE
            binding.lineLayout.alpha = 0f
            binding.lineLayout.animate().alpha(1f).start()

            binding.lineLayout2.visibility = View.VISIBLE
            binding.lineLayout2.alpha = 0f
            binding.lineLayout2.animate().alpha(1f).start()

            binding.textViewMSB.visibility = View.VISIBLE
            binding.textViewMSB.alpha = 0f
            binding.textViewMSB.animate().alpha(1f).start()

            binding.verseTV.visibility = View.VISIBLE
            binding.verseTV.alpha = 0f
            binding.verseTV.animate().alpha(1f).start()

            sharedPrefs.edit().putBoolean("databaseStatement", true).apply()
        }

    }
}