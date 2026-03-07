package com.hpnightowl.wardrobe.di

import android.content.Context
import androidx.room.Room
import com.hpnightowl.wardrobe.data.local.WardrobeDatabase
import com.hpnightowl.wardrobe.data.local.dao.ItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWardrobeDatabase(
        @ApplicationContext context: Context
    ): WardrobeDatabase {
        return Room.databaseBuilder(
            context,
            WardrobeDatabase::class.java,
            WardrobeDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideItemDao(database: WardrobeDatabase): ItemDao {
        return database.itemDao
    }
}
