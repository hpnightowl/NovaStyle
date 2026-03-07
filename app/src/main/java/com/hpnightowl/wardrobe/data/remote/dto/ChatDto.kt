package com.hpnightowl.wardrobe.data.remote.dto

import com.hpnightowl.wardrobe.domain.model.WardrobeItem

data class ChatRequestDto(
    val message: String,
    val wardrobe: List<WardrobeItem>,
    val userProfile: UserProfileDto?
)

data class ChatOutfitDto(
    val topId: String?,
    val bottomId: String?,
    val shoesId: String?
)

data class ChatResponseDto(
    val success: Boolean,
    val reply: String?,
    val outfits: List<ChatOutfitDto>?,
    val error: String?
)
