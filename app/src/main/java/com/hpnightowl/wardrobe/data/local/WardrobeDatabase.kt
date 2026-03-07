package com.hpnightowl.wardrobe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import com.hpnightowl.wardrobe.data.local.entity.WardrobeItemEntity

@Database(entities = [WardrobeItemEntity::class], version = 2, exportSchema = false)
abstract class WardrobeDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao

    companion object {
        const val DATABASE_NAME = "wardrobe_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new 'name' column with a default value to prevent null constraint errors
                database.execSQL("ALTER TABLE wardrobe_items ADD COLUMN name TEXT NOT NULL DEFAULT 'Unknown Item'")
            }
        }
    }
}
