package com.example.foodienow.core.di

import android.content.Context
import androidx.room.Room
import com.example.foodienow.data.local.room.AppDatabase
import com.example.foodienow.data.local.room.CartDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "foodienow_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCartDao(appDatabase: AppDatabase): CartDao {
        return appDatabase.cartDao()
    }
}
