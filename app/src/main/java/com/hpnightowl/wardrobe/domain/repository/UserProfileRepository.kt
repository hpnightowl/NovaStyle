package com.hpnightowl.wardrobe.domain.repository

import com.hpnightowl.wardrobe.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
}
