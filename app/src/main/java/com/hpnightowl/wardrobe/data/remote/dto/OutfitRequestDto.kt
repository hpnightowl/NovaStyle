package com.hpnightowl.wardrobe.data.remote.dto

// These are simplified DTOs representing what we expect from your AWS API Gateway

data class OutfitRequestDto(
    val latitude: Double,
    val longitude: Double,
    val currentWardrobeIds: List<String>
)

data class OutfitResponseDto(
    val success: Boolean,
    val topId: String,
    val bottomId: String,
    val shoesId: String,
    val weatherCondition: String,
    val temperatureCelsius: Double,
    val aiReasoning: String
)
