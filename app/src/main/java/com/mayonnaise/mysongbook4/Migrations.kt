package com.mayonnaise.mysongbook4

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new column to the table
        database.execSQL("ALTER TABLE `songs-table` ADD COLUMN `strophes` INTEGER NOT NULL DEFAULT 0")
    }
}