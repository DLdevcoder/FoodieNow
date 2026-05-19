package com.example.foodienow.domain.payment

data class PaymentTotals(
    val subtotal: Long,
    val deliveryFee: Long,
    val discountAmount: Long,
    val pointsDiscount: Long,
    val amountCharged: Long
)

object PaymentTotalsCalculator {
    const val FREE_DELIVERY_THRESHOLD: Long = 100_000L
    const val STANDARD_DELIVERY_FEE: Long = 15_000L

    fun calculate(
        subtotal: Long,
        voucherDiscount: Long,
        rewardPointsAvailable: Int,
        useRewardPoints: Boolean
    ): PaymentTotals {
        val normalizedSubtotal = subtotal.coerceAtLeast(0L)
        val deliveryFee = if (normalizedSubtotal > FREE_DELIVERY_THRESHOLD) {
            0L
        } else {
            STANDARD_DELIVERY_FEE
        }
        val discountAmount = voucherDiscount.coerceIn(0L, normalizedSubtotal)
        val payableBeforePoints = (normalizedSubtotal + deliveryFee - discountAmount).coerceAtLeast(0L)
        val pointsDiscount = if (useRewardPoints) {
            rewardPointsAvailable.toLong().coerceIn(0L, payableBeforePoints)
        } else {
            0L
        }

        return PaymentTotals(
            subtotal = normalizedSubtotal,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            pointsDiscount = pointsDiscount,
            amountCharged = (payableBeforePoints - pointsDiscount).coerceAtLeast(0L)
        )
    }
}
