package com.hpnightowl.wardrobe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import com.hpnightowl.wardrobe.data.local.entity.WardrobeItemEntity

@Database(entities = [WardrobeItemEntity::class], version = 1, exportSchema = false)
abstract class WardrobeDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao

    companion object {
        const val DATABASE_NAME = "wardrobe_db"
    }
}
