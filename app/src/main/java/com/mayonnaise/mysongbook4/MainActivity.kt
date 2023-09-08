package com.mayonnaise.mysongbook4

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
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
import com.mayonnaise.mysongbook4.databinding.ActivityMainBinding
import com.mayonnaise.mysongbook4.databinding.SettingsDialogBinding
import com.polyak.iconswitch.BuildConfig
import com.polyak.iconswitch.IconSwitch
import kotlinx.coroutines.Dispatchers
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

        val verseDao = SongbookDatabase.getInstance(this).verseDao()

        var data = arrayListOf<Int>()

        val sharedPrefs by lazy {
            getSharedPreferences(
                "${BuildConfig.APPLICATION_ID}_sharedPreferences",
                Context.MODE_PRIVATE)
        }

        val uiMode = sharedPrefs.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_NO)

        DataManager.textStyle = sharedPrefs.getInt("textStyle", Typeface.NORMAL)
        DataManager.textSize = sharedPrefs.getFloat("textSize", 20.0F)
        DataManager.textAlignment = sharedPrefs.getInt("textAlignment", View.TEXT_ALIGNMENT_TEXT_START)

        AppCompatDelegate.setDefaultNightMode(uiMode)

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                data = arrayListOf(R.drawable.duchowe_light, R.drawable.wedrowiec_light, R.drawable.bialy_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                data = arrayListOf(R.drawable.duchowe_dark, R.drawable.wedrowiec_dark, R.drawable.bialy_dark)
            }
        }


        fun setTextAttributes(){
            binding.buttonReport.textSize = DataManager.textSize-5
            binding.buttonReport.setTypeface(null, DataManager.textStyle)

            binding.buttonSettings.textSize = DataManager.textSize-5
            binding.buttonSettings.setTypeface(null, DataManager.textStyle)

            binding.verseTV.textSize = DataManager.textSize
            binding.verseTV.setTypeface(null, DataManager.textStyle)

            binding.versePlaceTV.textSize = DataManager.textSize-5
            binding.versePlaceTV.setTypeface(null, DataManager.textStyle)

            binding.textViewMSB.textSize = DataManager.textSize-8

        }

        fun initializeActivity (){
            lifecycleScope.launch(Dispatchers.IO){
                val verseID = Random.nextInt(1, 39)
                val verse = verseDao.getVerse(verseID)

                withContext(Dispatchers.Main){

                    if (sharedPrefs.getBoolean("databaseStatement", false)){

                        binding.textViewMSB.visibility = View.VISIBLE
                        binding.textViewMSB.alpha = 0f
                        binding.textViewMSB.animate().alpha(1f).start()

                        binding.lineLayout.visibility = View.VISIBLE
                        binding.lineLayout.alpha = 0f
                        binding.lineLayout.animate().alpha(1f).start()

                        binding.lineLayout2.visibility = View.VISIBLE
                        binding.lineLayout2.alpha = 0f
                        binding.lineLayout2.animate().alpha(1f).start()

                        binding.versePlaceTV.animate()
                            .alpha(0f)
                            .setDuration(200L)
                            .withEndAction {
                                binding.versePlaceTV.visibility = View.VISIBLE
                                binding.versePlaceTV.text = verse.place
                                binding.versePlaceTV.animate()
                                    .alpha(1f)
                                    .setDuration(200L)
                                    .start()
                            }
                            .start()

                        binding.verseTV.animate()
                            .alpha(0f)
                            .setDuration(200L)
                            .withEndAction {
                                binding.verseTV.visibility = View.VISIBLE
                                binding.verseTV.text = verse.text
                                binding.verseTV.animate()
                                    .alpha(1f)
                                    .setDuration(200L)
                                    .start()
                            }
                            .start()

                        binding.viewPager.animate()
                            .alpha(0f)
                            .setDuration(200L)
                            .withEndAction {
                                binding.viewPager.adapter = SongbookViewPagerAdapter(data, lifecycleScope)
                                binding.viewPager.visibility = View.VISIBLE
                                binding.viewPager.animate()
                                    .alpha(1f)
                                    .setDuration(200L)
                                    .start()
                            }
                            .start()
                    }
                    else{
                        binding.progressBar.visibility = View.VISIBLE
                        binding.initializeTV.visibility = View.VISIBLE

                        SongParser.initialize(this@MainActivity, lifecycleScope)

                        binding.initializeTV.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE

                        binding.versePlaceTV.visibility = View.GONE

                        binding.textViewMSB.visibility = View.VISIBLE
                        binding.textViewMSB.alpha = 0f
                        binding.textViewMSB.animate().alpha(1f).start()

                        binding.lineLayout.visibility = View.VISIBLE
                        binding.lineLayout.alpha = 0f
                        binding.lineLayout.animate().alpha(1f).start()

                        binding.lineLayout2.visibility = View.VISIBLE
                        binding.lineLayout2.alpha = 0f
                        binding.lineLayout2.animate().alpha(1f).start()

                        binding.viewPager.animate()
                            .alpha(0f)
                            .setDuration(200L)
                            .withEndAction {
                                binding.viewPager.adapter = SongbookViewPagerAdapter(data, lifecycleScope)
                                binding.viewPager.visibility = View.VISIBLE
                                binding.viewPager.animate()
                                    .alpha(1f)
                                    .setDuration(200L)
                                    .start()
                            }
                            .start()

                        binding.verseTV.animate()
                            .alpha(0f)
                            .setDuration(200L)
                            .withEndAction {
                                binding.verseTV.visibility = View.VISIBLE
                                binding.verseTV.text = "Witamy w MySongbook! Zapraszamy do zapoznania się z wszystkimi funkcjami naszej aplikacji :)"
                                binding.verseTV.animate()
                                    .alpha(1f)
                                    .setDuration(200L)
                                    .start()
                            }
                            .start()

                        sharedPrefs.edit().putBoolean("databaseStatement", true).apply()
                    }
                }
            }
        }

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

        binding.buttonReport.setOnClickListener{
            DataManager.isSongReported = false
            val sendReportActivity = Intent(this, SendReportActivity::class.java)
            this.startActivity(sendReportActivity)

        }

        binding.buttonSettings.setOnClickListener {
            val settingsDialog = Dialog(this)
            val dialogBinding = SettingsDialogBinding.inflate(layoutInflater)
            settingsDialog.setContentView(dialogBinding.root)

            dialogBinding.sliderTextSize.value = DataManager.textSize

            dialogBinding.textView2.textSize = DataManager.textSize-2
            dialogBinding.textView3.textSize = DataManager.textSize-2
            dialogBinding.textView4.textSize = DataManager.textSize-2
            dialogBinding.textView5.textSize = DataManager.textSize-2

            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    dialogBinding.nightModeSwitch.checked = IconSwitch.Checked.RIGHT
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    dialogBinding.nightModeSwitch.checked = IconSwitch.Checked.LEFT
                }
            }

            when (sharedPrefs.getInt("textStyle", Typeface.NORMAL)) {
                Typeface.BOLD -> {
                    dialogBinding.textModeSwitch.checked = IconSwitch.Checked.RIGHT
                }
                Typeface.NORMAL -> {
                    dialogBinding.textModeSwitch.checked = IconSwitch.Checked.LEFT
                }
            }

            when (sharedPrefs.getInt("textAlignment", View.TEXT_ALIGNMENT_TEXT_START)) {
                View.TEXT_ALIGNMENT_CENTER -> {
                    dialogBinding.textAlignmentSwitch.checked = IconSwitch.Checked.RIGHT
                }
                View.TEXT_ALIGNMENT_TEXT_START -> {
                    dialogBinding.textAlignmentSwitch.checked = IconSwitch.Checked.LEFT
                }
            }

            dialogBinding.nightModeSwitch.setCheckedChangeListener { current ->
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

            dialogBinding.textAlignmentSwitch.setCheckedChangeListener { current ->
                var newTextAlignment = DataManager.textAlignment

                when (current) {
                    IconSwitch.Checked.LEFT -> {
                        newTextAlignment = View.TEXT_ALIGNMENT_TEXT_START
                        DataManager.textAlignment = newTextAlignment
                    }

                    IconSwitch.Checked.RIGHT -> {
                        newTextAlignment = View.TEXT_ALIGNMENT_CENTER
                        DataManager.textAlignment = newTextAlignment
                    }

                    else -> Toast.makeText(this@MainActivity, "ERROR 2137", Toast.LENGTH_SHORT).show()
                }

                sharedPrefs.edit().putInt("textAlignment", newTextAlignment).apply()
            }

            dialogBinding.textModeSwitch.setCheckedChangeListener { current ->
                var newTextStyle = DataManager.textStyle

                when (current) {
                    IconSwitch.Checked.LEFT -> {
                        newTextStyle = Typeface.NORMAL
                        DataManager.textStyle = newTextStyle
                        setTextAttributes()
                    }

                    IconSwitch.Checked.RIGHT -> {
                        newTextStyle = Typeface.BOLD
                        DataManager.textStyle = newTextStyle
                        setTextAttributes()
                    }

                    else -> Toast.makeText(this@MainActivity, "ERROR 2137", Toast.LENGTH_SHORT).show()
                }

                sharedPrefs.edit().putInt("textStyle", newTextStyle).apply()
            }

            dialogBinding.sliderTextSize.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
                dialogBinding.textView2.textSize = value-2
                dialogBinding.textView3.textSize = value-2
                dialogBinding.textView4.textSize = value-2
                dialogBinding.textView5.textSize = value-2

                binding.verseTV.textSize = value
                binding.versePlaceTV.textSize = value-5
                binding.buttonReport.textSize = value-5
                binding.buttonSettings.textSize = value-5
                binding.textViewMSB.textSize = value-8

                DataManager.textSize = value
                sharedPrefs.edit().putFloat("textSize", dialogBinding.sliderTextSize.value).apply()
            })
            settingsDialog.show()
        }

        initializeActivity()
        setTextAttributes()
    }
}