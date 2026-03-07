package com.hpnightowl.wardrobe.domain.repository

import com.hpnightowl.wardrobe.domain.model.Outfit

interface OutfitRepository {
    /**
     * Ask the AI to generate an outfit based on current wardrobe items and location/weather.
     */
    suspend fun generateOutfitForToday(latitude: Double, longitude: Double): Result<Outfit>
}
