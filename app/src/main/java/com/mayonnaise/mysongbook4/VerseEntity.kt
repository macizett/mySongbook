package com.mayonnaise.mysongbook4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verses-table")
data class VerseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val place: String,
    val text: String
)
