package com.example.foodienow.domain.payment

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentTotalsCalculatorTest {

    @Test
    fun belowFreeDeliveryThreshold_appliesStandardDeliveryFee() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 80_000L,
            voucherDiscount = 0L,
            rewardPointsAvailable = 0,
            useRewardPoints = false
        )

        assertEquals(80_000L, totals.subtotal)
        assertEquals(15_000L, totals.deliveryFee)
        assertEquals(95_000L, totals.amountCharged)
    }

    @Test
    fun aboveFreeDeliveryThreshold_removesDeliveryFee() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 120_000L,
            voucherDiscount = 0L,
            rewardPointsAvailable = 0,
            useRewardPoints = false
        )

        assertEquals(0L, totals.deliveryFee)
        assertEquals(120_000L, totals.amountCharged)
    }

    @Test
    fun voucherDiscount_isCappedAtSubtotal() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 40_000L,
            voucherDiscount = 50_000L,
            rewardPointsAvailable = 0,
            useRewardPoints = false
        )

        assertEquals(40_000L, totals.discountAmount)
        assertEquals(15_000L, totals.amountCharged)
    }

    @Test
    fun rewardPoints_canReducePayableAmountToZero() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 80_000L,
            voucherDiscount = 20_000L,
            rewardPointsAvailable = 75_000,
            useRewardPoints = true
        )

        assertEquals(15_000L, totals.deliveryFee)
        assertEquals(20_000L, totals.discountAmount)
        assertEquals(75_000L, totals.pointsDiscount)
        assertEquals(0L, totals.amountCharged)
    }
}
