package com.mayonnaise.mysongbook

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SongInterfaceDao {
    @Insert
    fun insert(songEntity: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(songs: List<SongEntity>)

    @Update
    fun updateFavoriteSongs(song: SongEntity)

    @Query("SELECT * FROM `songs-table` WHERE isFavorite = 1 AND songbook=:songbook")
    fun getFavoriteSongs(songbook: Int): List<SongEntity>

    @Query("SELECT * FROM `songs-table` WHERE songbook=:songbook")
    fun getAllSongsBySongbook(songbook: Int): List<SongEntity>

    @Query("SELECT * FROM `songs-table` WHERE number=:number AND songbook=:songbook")
    fun getSongByNumber(number: Int, songbook: Int): SongEntity

    @Query("SELECT * FROM `songs-table` WHERE text LIKE '%' || :searchPhrase || '%' AND songbook=:songbook")
    fun searchForPhrase(searchPhrase: String, songbook: Int): List<SongEntity>

    @Query("SELECT * FROM `songs-table` WHERE textNormalized LIKE '%' || :searchPhrase || '%' AND songbook=:songbook")
    fun searchForPhraseWithoutMarks(searchPhrase: String, songbook: Int): List<SongEntity>
}