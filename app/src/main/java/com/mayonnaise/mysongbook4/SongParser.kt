package com.mayonnaise.mysongbook4

import android.content.Context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object SongParser {
    var id: Long = 1
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }

    suspend fun initialize(context: Context, isInitialized: Boolean) {
        if (!isInitialized) {
           GlobalScope.launch(Dispatchers.Default + coroutineExceptionHandler){
                parseAndInsertSongs(context, "duchowe.json", 1)
                parseAndInsertSongs(context, "wedrowiec.json", 2)
                parseAndInsertSongs(context, "bialy.json", 3)
                parseAndInsertVerses(context, "verses.json")
            }
        }
    }

    private suspend fun parseAndInsertSongs(context: Context, jsonFileName: String, songbook: Int) {
        val dao = SongbookDatabase.getInstance(context).songDao()

            var jsonString = context.assets.open(jsonFileName).bufferedReader().use {
                it.readText()}

            var songsJsonArray = JSONArray(jsonString)

            var songsList = mutableListOf<SongEntity>()

            songsList.clear()

            for (i in 0 until songsJsonArray.length()) {
                var songJsonObject: JSONObject = songsJsonArray.getJSONObject(i)
                var title = songJsonObject.getString("title")
                var text = songJsonObject.getString("text")
                var number = songJsonObject.getInt("number")
                var textNormalized = text.replace(Regex("[,.]"), "")

                var song = SongEntity(title = title, text = text, textNormalized = textNormalized, number = number, songbook = songbook, id = id, isFavorite = false)
                songsList.add(song)
                id++
            }


            withContext(Dispatchers.IO) {
                dao.insertAll(songsList)
            }
    }

    private suspend fun parseAndInsertVerses(context: Context, jsonFileName: String) {
        val dao = SongbookDatabase.getInstance(context).verseDao()

        var jsonString = context.assets.open(jsonFileName).bufferedReader().use {
            it.readText()}

        var versesJsonArray = JSONArray(jsonString)

        var verseList = mutableListOf<VerseEntity>()

        verseList.clear()

        for (i in 0 until versesJsonArray.length()) {
            var songJsonObject: JSONObject = versesJsonArray.getJSONObject(i)
            var id = songJsonObject.getInt("id")
            var place = songJsonObject.getString("place")
            var text = songJsonObject.getString("text")

            var song = VerseEntity(id = id, place = place, text = text)
            verseList.add(song)
            id++
        }


        withContext(Dispatchers.IO) {
            dao.insertAll(verseList)
        }
    }

}