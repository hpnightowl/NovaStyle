package com.hpnightowl.wardrobe.data.remote.dto

// These are simplified DTOs representing what we expect from your AWS API Gateway

data class OutfitRequestDto(
    val latitude: Double,
    val longitude: Double,
    val currentWardrobeItems: List<WardrobeItemDto>,
    val userProfile: UserProfileDto? = null
)

data class WardrobeItemDto(
    val id: String,
    val category: String,
    val color: String,
    val style: String
)

data class UserProfileDto(
    val skinTone: String,
    val palette: String
)

data class OutfitResponseDto(
    val success: Boolean,
    val topId: String,
    val bottomId: String,
    val shoesId: String,
    val weatherCondition: String,
    val temperatureCelsius: Double,
    val locationName: String?,
    val aiReasoning: String
)
