package com.hpnightowl.wardrobe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hpnightowl.wardrobe.domain.model.WardrobeItem

@Entity(tableName = "wardrobe_items")
data class WardrobeItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val color: String,
    val style: String
)

// Extension functions for mapping between Domain and Data models
fun WardrobeItemEntity.toDomainModel(): WardrobeItem {
    return WardrobeItem(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        color = color,
        style = style
    )
}

fun WardrobeItem.toEntity(): WardrobeItemEntity {
    return WardrobeItemEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        color = color,
        style = style
    )
}
