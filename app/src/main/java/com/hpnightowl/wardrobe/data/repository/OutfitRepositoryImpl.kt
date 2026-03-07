package com.hpnightowl.wardrobe.data.repository

import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import com.hpnightowl.wardrobe.data.local.entity.toDomainModel
import com.hpnightowl.wardrobe.data.remote.AwsOutfitApi
import com.hpnightowl.wardrobe.data.remote.dto.OutfitRequestDto
import com.hpnightowl.wardrobe.domain.model.Outfit
import com.hpnightowl.wardrobe.domain.model.Weather
import com.hpnightowl.wardrobe.domain.repository.OutfitRepository
import com.hpnightowl.wardrobe.domain.repository.UserProfileRepository
import com.hpnightowl.wardrobe.data.remote.dto.UserProfileDto
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of [OutfitRepository].
 * Coordinates between the local database (to get current items) and the remote API (Amazon Nova).
 */
class OutfitRepositoryImpl @Inject constructor(
    private val api: AwsOutfitApi,
    private val itemDao: ItemDao,
    private val userProfileRepository: UserProfileRepository
) : OutfitRepository {

    override suspend fun generateOutfitForToday(
        latitude: Double,
        longitude: Double
    ): Result<Outfit> {
        return try {
            val currentItems = itemDao.getAllItems().first().map { it.toDomainModel() }
            val userProfile = userProfileRepository.getUserProfile().first()
            
            val request = OutfitRequestDto(
                latitude = latitude,
                longitude = longitude,
                currentWardrobeItems = currentItems.map { item ->
                    com.hpnightowl.wardrobe.data.remote.dto.WardrobeItemDto(
                        id = item.id,
                        category = item.category,
                        color = item.color,
                        style = item.style
                    )
                },
                userProfile = userProfile?.let { UserProfileDto(it.skinTone, it.palette) }
            )

            val response = api.generateOutfitForToday(request)

            if (!response.success) {
                return Result.failure(Exception("Failed to generate outfit."))
            }

            val top = currentItems.find { it.id == response.topId }
            val bottom = currentItems.find { it.id == response.bottomId }
            val shoes = currentItems.find { it.id == response.shoesId }

            if (top == null && bottom == null && shoes == null) {
                return Result.failure(Exception("AI failed to suggest any items from the wardrobe."))
            }

            val outfit = Outfit(
                id = java.util.UUID.randomUUID().toString(),
                top = top,
                bottom = bottom,
                shoes = shoes,
                weatherTarget = Weather(
                    temperatureCelsius = response.temperatureCelsius,
                    condition = response.weatherCondition,
                    locationName = response.locationName
                ),
                aiReasoning = response.aiReasoning
            )

            Result.success(outfit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
