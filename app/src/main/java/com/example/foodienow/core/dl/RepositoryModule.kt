package com.example.foodienow.core.dl

import com.example.foodienow.data.repository.FoodRepositoryImpl
import com.example.foodienow.data.repository.AuthRepositoryImpl
import com.example.foodienow.data.repository.MerchantRepositoryImpl
import com.example.foodienow.data.repository.NotificationRepositoryImpl
import com.example.foodienow.data.repository.OrderRepositoryImpl
import com.example.foodienow.data.repository.PaymentRepositoryImpl
import com.example.foodienow.data.repository.ProfileRepositoryImpl
import com.example.foodienow.data.repository.MockVoucherRepository
import com.example.foodienow.data.repository.ReviewRepositoryImpl
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.CustomerFoodRepository
import com.example.foodienow.domain.repository.MerchantRepository
import com.example.foodienow.domain.repository.NotificationRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.ProfileRepository
import com.example.foodienow.domain.repository.ReviewRepository
import com.example.foodienow.domain.repository.VoucherRepository
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

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindVoucherRepository(
        impl: MockVoucherRepository
    ): VoucherRepository
}
