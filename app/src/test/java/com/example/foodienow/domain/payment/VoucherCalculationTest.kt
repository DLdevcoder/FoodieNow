package com.example.foodienow.domain.payment

import com.example.foodienow.domain.model.Voucher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoucherCalculationTest {

    private fun calculateVoucherDiscount(voucher: Voucher, subtotal: Long): Long {
        if (subtotal < voucher.minOrderValue) return 0L
        val rawDiscount = if (voucher.discountAmount > 0L) {
            voucher.discountAmount
        } else {
            kotlin.math.floor(subtotal * voucher.discountPercent / 100.0).toLong()
        }
        val cappedDiscount = if (voucher.maxDiscount > 0L) {
            kotlin.math.min(rawDiscount, voucher.maxDiscount)
        } else {
            rawDiscount
        }
        return cappedDiscount.coerceIn(0L, subtotal)
    }

    private fun findBestVoucher(vouchers: List<Voucher>, subtotal: Long): Voucher? {
        var bestVoucher: Voucher? = null
        var maxDiscount = 0L
        for (voucher in vouchers) {
            if (subtotal >= voucher.minOrderValue) {
                val discount = calculateVoucherDiscount(voucher, subtotal)
                if (discount > maxDiscount) {
                    maxDiscount = discount
                    bestVoucher = voucher
                }
            }
        }
        return bestVoucher
    }

    @Test
    fun fixedAmountVoucher_calculatesCorrectly() {
        val voucher = Voucher(
            id = "1",
            merchantId = "m1",
            code = "FIXED50",
            discountPercent = 0,
            maxDiscount = 0L,
            minOrderValue = 100_000L,
            discountAmount = 50_000L,
            isActive = true
        )

        assertEquals(0L, calculateVoucherDiscount(voucher, 80_000L))
        assertEquals(50_000L, calculateVoucherDiscount(voucher, 120_000L))

        val voucherCap = Voucher(
            id = "1b",
            merchantId = "m1",
            code = "FIXED_CAP",
            discountPercent = 0,
            maxDiscount = 0L,
            minOrderValue = 30_000L,
            discountAmount = 50_000L,
            isActive = true
        )
        assertEquals(40_000L, calculateVoucherDiscount(voucherCap, 40_000L))
    }

    @Test
    fun percentVoucher_calculatesAndCapsCorrectly() {
        val voucher = Voucher(
            id = "2",
            merchantId = "m1",
            code = "PERCENT20",
            discountPercent = 20,
            maxDiscount = 30_000L,
            minOrderValue = 80_000L,
            discountAmount = 0L,
            isActive = true
        )

        assertEquals(0L, calculateVoucherDiscount(voucher, 50_000L))
        assertEquals(20_000L, calculateVoucherDiscount(voucher, 100_000L))
        assertEquals(30_000L, calculateVoucherDiscount(voucher, 200_000L))
    }

    @Test
    fun findBestVoucher_selectsHighestDiscount() {
        val v1 = Voucher(
            id = "1",
            merchantId = "m1",
            code = "FIXED20",
            discountPercent = 0,
            maxDiscount = 0L,
            minOrderValue = 50_000L,
            discountAmount = 20_000L,
            isActive = true
        )
        val v2 = Voucher(
            id = "2",
            merchantId = "m1",
            code = "PERCENT10",
            discountPercent = 10,
            maxDiscount = 50_000L,
            minOrderValue = 100_000L,
            discountAmount = 0L,
            isActive = true
        )
        val v3 = Voucher(
            id = "3",
            merchantId = "m1",
            code = "HIGH_MIN",
            discountPercent = 50,
            maxDiscount = 100_000L,
            minOrderValue = 500_000L,
            discountAmount = 0L,
            isActive = true
        )

        val list = listOf(v1, v2, v3)

        assertEquals(v1, findBestVoucher(list, 80_000L))
        assertEquals(v2, findBestVoucher(list, 250_000L))
        assertEquals(v3, findBestVoucher(list, 600_000L))
        assertNull(findBestVoucher(list, 40_000L))
    }
}
