package com.mayonnaise.mysongbook4

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class, VerseEntity::class], version = 2)
abstract class SongbookDatabase: RoomDatabase() {

    abstract fun songDao(): SongInterfaceDao
    abstract fun verseDao(): VerseInterfaceDao

    companion object {
        @Volatile
        private var INSTANCE: SongbookDatabase? = null

        fun getInstance(context: Context): SongbookDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongbookDatabase::class.java,
                        "songbook_database"
                    ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}