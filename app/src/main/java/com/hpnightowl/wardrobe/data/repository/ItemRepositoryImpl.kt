package com.hpnightowl.wardrobe.data.repository

import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import com.hpnightowl.wardrobe.data.local.entity.toDomainModel
import com.hpnightowl.wardrobe.data.local.entity.toEntity
import com.hpnightowl.wardrobe.domain.model.WardrobeItem
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of [ItemRepository] from the Domain layer.
 * This class abstracts the Room database away from the rest of the app.
 */
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getAllItems(): Flow<List<WardrobeItem>> {
        return itemDao.getAllItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveItem(item: WardrobeItem) {
        itemDao.insertItem(item.toEntity())
    }

    override suspend fun deleteItem(item: WardrobeItem) {
        itemDao.deleteItem(item.toEntity())
    }
}
