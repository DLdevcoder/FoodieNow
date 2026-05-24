package com.example.foodienow.core.dl

import com.example.foodienow.data.payment.RealWalletPaymentGateway
import com.example.foodienow.domain.payment.WalletPaymentGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentGatewayModule {
    @Provides
    @Singleton
    fun provideWalletPaymentGateway(): WalletPaymentGateway = RealWalletPaymentGateway()
}
