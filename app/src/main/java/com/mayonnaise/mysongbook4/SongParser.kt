package com.mayonnaise.mysongbook4

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object SongParser {
    var id: Long = 1
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }

    fun initialize(context: Context, lifecycle: LifecycleCoroutineScope, initializeLayout: LinearLayout) {
        initializeLayout.visibility = View.VISIBLE

        lifecycle.launch(Dispatchers.Default + coroutineExceptionHandler){
               delay(1_000)
               parseAndInsertSongs(context, "duchowe.json", 1)
               parseAndInsertSongs(context, "wedrowiec.json", 2)
               parseAndInsertSongs(context, "bialy.json", 3)
               parseAndInsertVerses(context, "verses.json")
               withContext(Dispatchers.Main){
                   initializeLayout.visibility = View.GONE
               }
            }
    }

    private suspend fun parseAndInsertSongs(context: Context, jsonFileName: String, songbook: Int) {
        val dao = SongbookDatabase.getInstance(context).songDao()

            val jsonString = context.assets.open(jsonFileName).bufferedReader().use {
                it.readText()}

            val songsJsonArray = JSONArray(jsonString)

            val songsList = mutableListOf<SongEntity>()

            songsList.clear()

            for (i in 0 until songsJsonArray.length()) {
                val songJsonObject: JSONObject = songsJsonArray.getJSONObject(i)
                val title = songJsonObject.getString("title")
                val text = songJsonObject.getString("text")
                val number = songJsonObject.getInt("number")
                val strophes = songJsonObject.getInt("strophes")
                val textNormalized = text.replace(Regex("[,.]"), "")

                val song = SongEntity(title = title, text = text, textNormalized = textNormalized, number = number, songbook = songbook, id = id, strophes = strophes ,isFavorite = false)
                songsList.add(song)
                id++
            }


            withContext(Dispatchers.IO) {
                dao.insertAll(songsList)
            }
    }

    private suspend fun parseAndInsertVerses(context: Context, jsonFileName: String) {
        val dao = SongbookDatabase.getInstance(context).verseDao()

        val jsonString = context.assets.open(jsonFileName).bufferedReader().use {
            it.readText()}

        val versesJsonArray = JSONArray(jsonString)

        val verseList = mutableListOf<VerseEntity>()

        verseList.clear()

        for (i in 0 until versesJsonArray.length()) {
            val songJsonObject: JSONObject = versesJsonArray.getJSONObject(i)
            var id = songJsonObject.getInt("id")
            val place = songJsonObject.getString("place")
            val text = songJsonObject.getString("text")

            val song = VerseEntity(id = id, place = place, text = text)
            verseList.add(song)
            id++
        }


        withContext(Dispatchers.IO) {
            dao.insertAll(verseList)
        }
    }

}