package com.example.foodienow.domain.repository

/**
 * Repository for voucher / promo code validation.
 */
interface VoucherRepository {
    /**
     * Validates a voucher code and returns the discount amount.
     * Returns 0 if the code is invalid or expired.
     */
    suspend fun getDiscount(code: String): Long
}