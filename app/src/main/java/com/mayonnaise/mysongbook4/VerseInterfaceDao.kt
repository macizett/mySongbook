package com.mayonnaise.mysongbook4

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VerseInterfaceDao {
    @Insert
    fun insertAll(verses: List<VerseEntity>)

    @Query("SELECT * FROM `verses-table` WHERE id=:id")
    fun getVerse(id: Int): VerseEntity
}