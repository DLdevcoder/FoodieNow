package com.example.foodienow.data.repository

import com.example.foodienow.domain.repository.VoucherRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock voucher repository with hardcoded promo codes.
 * Replace with a real Supabase-backed implementation when the
 * vouchers table is created.
 */
@Singleton
class MockVoucherRepository @Inject constructor() : VoucherRepository {

    private val vouchers: Map<String, Long> = mapOf(
        "GIAM20K" to 20_000L,
        "FREESHIP" to 15_000L,
        "WELCOME50" to 50_000L,
        "FOODIE10" to 10_000L
    )

    override suspend fun getDiscount(code: String): Long {
        return vouchers[code.uppercase().trim()] ?: 0L
    }
}