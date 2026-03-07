package com.hpnightowl.wardrobe.domain.repository

import com.hpnightowl.wardrobe.domain.model.WardrobeItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    /**
     * Get all items currently saved in the wardrobe.
     * Returns a Flow to automatically emit updates when items change.
     */
    fun getAllItems(): Flow<List<WardrobeItem>>

    /**
     * Save a new item to the wardrobe.
     */
    suspend fun saveItem(item: WardrobeItem)

    /**
     * Delete an item from the wardrobe.
     */
    suspend fun deleteItem(item: WardrobeItem)

    /**
     * Update an existing item in the wardrobe.
     */
    suspend fun updateItem(item: WardrobeItem)
}
