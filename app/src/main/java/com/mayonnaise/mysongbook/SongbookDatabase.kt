package com.mayonnaise.mysongbook

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [SongEntity::class], version = 1)
abstract class SongbookDatabase: RoomDatabase() {

    abstract fun songDao(): SongInterfaceDao

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
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
        }