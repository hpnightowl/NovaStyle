package com.hpnightowl.wardrobe.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hpnightowl.wardrobe.domain.model.UserProfile
import com.hpnightowl.wardrobe.domain.repository.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserProfileRepository {

    private val skinToneKey = stringPreferencesKey("skin_tone")
    private val paletteKey = stringPreferencesKey("palette")

    override fun getUserProfile(): Flow<UserProfile?> {
        return context.dataStore.data.map { preferences ->
            val skinTone = preferences[skinToneKey]
            val palette = preferences[paletteKey]
            if (skinTone != null && palette != null) {
                UserProfile(skinTone, palette)
            } else {
                null
            }
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[skinToneKey] = profile.skinTone
            preferences[paletteKey] = profile.palette
        }
    }
}
