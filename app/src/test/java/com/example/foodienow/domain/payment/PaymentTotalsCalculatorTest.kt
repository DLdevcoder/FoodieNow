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

    @Test
    fun voucherAndPointsCombined_reducesTotalCorrectly() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 120_000L,
            voucherDiscount = 20_000L,
            rewardPointsAvailable = 50_000,
            useRewardPoints = true
        )

        assertEquals(0L, totals.deliveryFee)
        assertEquals(20_000L, totals.discountAmount)
        assertEquals(50_000L, totals.pointsDiscount)
        assertEquals(50_000L, totals.amountCharged)
    }

    @Test
    fun pointsExceedPayable_capsPointsDiscountToPayable() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 50_000L,
            voucherDiscount = 10_000L,
            rewardPointsAvailable = 100_000,
            useRewardPoints = true
        )

        assertEquals(15_000L, totals.deliveryFee)
        assertEquals(10_000L, totals.discountAmount)
        assertEquals(55_000L, totals.pointsDiscount)
        assertEquals(0L, totals.amountCharged)
    }

    @Test
    fun negativeInputs_handledGracefully() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = -50_000L,
            voucherDiscount = -10_000L,
            rewardPointsAvailable = -500,
            useRewardPoints = true
        )

        assertEquals(0L, totals.subtotal)
        assertEquals(15_000L, totals.deliveryFee)
        assertEquals(0L, totals.discountAmount)
        assertEquals(0L, totals.pointsDiscount)
        assertEquals(15_000L, totals.amountCharged)
    }

    @Test
    fun exactThreshold_appliesStandardDeliveryFee() {
        val totals = PaymentTotalsCalculator.calculate(
            subtotal = 100_000L,
            voucherDiscount = 0L,
            rewardPointsAvailable = 0,
            useRewardPoints = false
        )

        assertEquals(100_000L, totals.subtotal)
        assertEquals(15_000L, totals.deliveryFee)
        assertEquals(115_000L, totals.amountCharged)
    }
}


