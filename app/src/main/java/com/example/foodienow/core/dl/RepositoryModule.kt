package com.example.foodienow.core.dl

import com.example.foodienow.data.repository.FoodRepositoryImpl
import com.example.foodienow.data.repository.AuthRepositoryImpl
import com.example.foodienow.data.repository.MerchantRepositoryImpl
import com.example.foodienow.data.repository.OrderRepositoryImpl
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.CustomerFoodRepository
import com.example.foodienow.domain.repository.MerchantRepository
import com.example.foodienow.domain.repository.OrderRepository
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
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: FoodRepositoryImpl
    ): CustomerFoodRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindMerchantRepository(
        impl: MerchantRepositoryImpl
    ): MerchantRepository
}
