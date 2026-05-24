package com.example.foodienow.feature.profile

import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.PaymentMethodCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentSettingsSelectionMapperTest {

    @Test
    fun fromOptionId_mapsWalletProvidersAndKeepsFallbackForOtherMethods() {
        val fallback = WalletProvider.MOMO

        val vnpay = PaymentSettingsSelectionMapper.fromOptionId("vnpay", fallback)
        val foodiePay = PaymentSettingsSelectionMapper.fromOptionId("foodie_pay", fallback)
        val cod = PaymentSettingsSelectionMapper.fromOptionId("cod", fallback)

        assertEquals(PaymentMethod.WALLET, vnpay?.method)
        assertEquals(WalletProvider.VNPAY, vnpay?.provider)

        assertEquals(PaymentMethod.FOODIE_PAY, foodiePay?.method)
        assertEquals(fallback, foodiePay?.provider)

        assertEquals(PaymentMethod.COD, cod?.method)
        assertEquals(fallback, cod?.provider)
    }

    @Test
    fun toOptionId_mapsSelectionsBackToIds() {
        assertEquals("momo", PaymentSettingsSelectionMapper.toOptionId(PaymentMethod.WALLET, WalletProvider.MOMO))
        assertEquals("paypal", PaymentSettingsSelectionMapper.toOptionId(PaymentMethod.WALLET, WalletProvider.PAYPAL))
        assertEquals("foodie_pay", PaymentSettingsSelectionMapper.toOptionId(PaymentMethod.FOODIE_PAY, WalletProvider.ZALOPAY))
        assertEquals("card", PaymentSettingsSelectionMapper.toOptionId(PaymentMethod.CARD, WalletProvider.GOOGLE_PLAY))
        assertEquals("cod", PaymentSettingsSelectionMapper.toOptionId(PaymentMethod.COD, null))
    }

    @Test
    fun paymentCatalog_onlyRequiresSetupForExternalMethods() {
        assertTrue(PaymentMethodCatalog.isOptionAvailable(PaymentMethodCatalog.COD_ID, emptySet()))
        assertTrue(PaymentMethodCatalog.isOptionAvailable(PaymentMethodCatalog.FOODIE_PAY_ID, emptySet()))

        assertFalse(PaymentMethodCatalog.isOptionAvailable(PaymentMethodCatalog.CARD_ID, emptySet()))
        assertFalse(PaymentMethodCatalog.isOptionAvailable(PaymentMethodCatalog.MOMO_ID, emptySet()))

        assertTrue(
            PaymentMethodCatalog.isOptionAvailable(
                PaymentMethodCatalog.CARD_ID,
                setOf(PaymentMethodCatalog.CARD_ID)
            )
        )
    }
}
