package com.hpnightowl.wardrobe.data.repository

import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import com.hpnightowl.wardrobe.data.local.entity.toDomainModel
import com.hpnightowl.wardrobe.data.remote.AwsOutfitApi
import com.hpnightowl.wardrobe.data.remote.dto.OutfitRequestDto
import com.hpnightowl.wardrobe.domain.model.Outfit
import com.hpnightowl.wardrobe.domain.model.Weather
import com.hpnightowl.wardrobe.domain.repository.OutfitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of [OutfitRepository].
 * Coordinates between the local database (to get current items) and the remote API (Amazon Nova).
 */
class OutfitRepositoryImpl @Inject constructor(
    private val api: AwsOutfitApi,
    private val itemDao: ItemDao
) : OutfitRepository {

    override suspend fun generateOutfitForToday(
        latitude: Double,
        longitude: Double
    ): Result<Outfit> {
        return try {
            val currentItems = itemDao.getAllItems().first().map { it.toDomainModel() }
            
            val request = OutfitRequestDto(
                latitude = latitude,
                longitude = longitude,
                currentWardrobeIds = currentItems.map { it.id }
            )

            val response = api.generateOutfitForToday(request)

            if (!response.success) {
                return Result.failure(Exception("Failed to generate outfit."))
            }

            val top = currentItems.find { it.id == response.topId }
            val bottom = currentItems.find { it.id == response.bottomId }
            val shoes = currentItems.find { it.id == response.shoesId }

            if (top == null || bottom == null || shoes == null) {
                return Result.failure(Exception("AI suggested an item that doesn't exist locally."))
            }

            val outfit = Outfit(
                id = java.util.UUID.randomUUID().toString(),
                top = top,
                bottom = bottom,
                shoes = shoes,
                weatherTarget = Weather(
                    temperatureCelsius = response.temperatureCelsius,
                    condition = response.weatherCondition
                ),
                aiReasoning = response.aiReasoning
            )

            Result.success(outfit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
