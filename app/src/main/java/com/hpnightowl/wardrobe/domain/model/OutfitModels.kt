package com.hpnightowl.wardrobe.domain.model

data class Weather(
    val temperatureCelsius: Double,
    val condition: String,
    val locationName: String? = null
)

data class Outfit(
    val id: String,
    val top: WardrobeItem?,
    val bottom: WardrobeItem?,
    val shoes: WardrobeItem?,
    val weatherTarget: Weather,
    val aiReasoning: String // e.g., "Because it is sunny, a light t-shirt and shorts are recommended."
)
