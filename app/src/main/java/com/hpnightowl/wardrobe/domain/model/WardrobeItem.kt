package com.hpnightowl.wardrobe.domain.model

data class WardrobeItem(
    val id: String,
    val imageUrl: String,
    val category: String, // e.g., "Top", "Bottom", "Shoes"
    val color: String,
    val style: String // e.g., "Casual", "Formal", "Winter"
)
