package com.mayonnaise.mysongbook4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs-table")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val textNormalized: String,
    val number: Int,
    val title: String,
    val songbook: Int,
    val strophes: Int,
    var isFavorite: Boolean
)
