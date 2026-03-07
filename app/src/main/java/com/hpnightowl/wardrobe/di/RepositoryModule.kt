package com.hpnightowl.wardrobe.di

import com.hpnightowl.wardrobe.data.repository.ItemRepositoryImpl
import com.hpnightowl.wardrobe.data.repository.OutfitRepositoryImpl
import com.hpnightowl.wardrobe.data.repository.UserProfileRepositoryImpl
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import com.hpnightowl.wardrobe.domain.repository.OutfitRepository
import com.hpnightowl.wardrobe.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        itemRepositoryImpl: ItemRepositoryImpl
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindOutfitRepository(
        outfitRepositoryImpl: OutfitRepositoryImpl
    ): OutfitRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
}
