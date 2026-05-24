package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Voucher

data class VoucherQuote(
    val code: String,
    val discountAmount: Long
)

interface VoucherRepository {
    suspend fun quoteDiscount(code: String, storeId: String, subtotal: Long): Result<VoucherQuote>
    suspend fun getVouchersByStore(storeId: String): Result<List<Voucher>>
    suspend fun getVouchersByMerchant(merchantId: String): Result<List<Voucher>>
    suspend fun createVoucher(voucher: Voucher): Result<Voucher>
    suspend fun updateVoucher(voucher: Voucher): Result<Voucher>
    suspend fun deleteVoucher(voucherId: String): Result<Unit>
}
