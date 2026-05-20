package com.example.foodienow.domain.repository

/**
 * Repository for voucher / promo code validation.
 */
data class VoucherQuote(
    val code: String,
    val discountAmount: Long
)

interface VoucherRepository {
    /**
     * Validates a voucher code for the current store/subtotal and returns the preview discount.
     * Returns a successful quote with 0 discount if the code is invalid or not applicable.
     */
    suspend fun quoteDiscount(code: String, storeId: String, subtotal: Long): Result<VoucherQuote>
}
