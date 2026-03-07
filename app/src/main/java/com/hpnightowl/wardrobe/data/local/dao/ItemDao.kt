package com.hpnightowl.wardrobe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hpnightowl.wardrobe.data.local.entity.WardrobeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM wardrobe_items")
    fun getAllItems(): Flow<List<WardrobeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WardrobeItemEntity)

    @Delete
    suspend fun deleteItem(item: WardrobeItemEntity)

    @Update
    suspend fun updateItem(item: WardrobeItemEntity)
}
