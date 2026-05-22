package com.example.foodienow.data.repository

import com.example.foodienow.data.remote.SupabaseRest
import com.example.foodienow.domain.model.Voucher
import com.example.foodienow.domain.repository.VoucherQuote
import com.example.foodienow.domain.repository.VoucherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor
import kotlin.math.min

@Singleton
class VoucherRepositoryImpl @Inject constructor() : VoucherRepository {

    override suspend fun quoteDiscount(
        code: String,
        storeId: String,
        subtotal: Long
    ): Result<VoucherQuote> = withContext(Dispatchers.IO) {
        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.isBlank() || storeId.isBlank() || subtotal <= 0L) {
            return@withContext Result.success(VoucherQuote(normalizedCode, 0L))
        }

        runCatching {
            val ownerId = fetchStoreOwnerId(storeId)
                ?: return@runCatching VoucherQuote(normalizedCode, 0L)
            val voucher = fetchVoucher(normalizedCode, ownerId)
                ?: return@runCatching VoucherQuote(normalizedCode, 0L)

            if (subtotal < voucher.minOrderValue) {
                return@runCatching VoucherQuote(normalizedCode, 0L)
            }

            val rawDiscount = if (voucher.discountAmount > 0L) {
                voucher.discountAmount
            } else {
                floor(subtotal * voucher.discountPercent / 100.0).toLong()
            }

            val cappedDiscount = if (voucher.maxDiscount > 0L) {
                min(rawDiscount, voucher.maxDiscount)
            } else {
                rawDiscount
            }

            VoucherQuote(
                code = normalizedCode,
                discountAmount = cappedDiscount.coerceIn(0L, subtotal)
            )
        }
    }

    override suspend fun getVouchersByStore(storeId: String): Result<List<Voucher>> = withContext(Dispatchers.IO) {
        if (storeId.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        runCatching {
            val ownerId = fetchStoreOwnerId(storeId) ?: return@runCatching emptyList()

            val response = SupabaseRest.get(
                "/rest/v1/vouchers?select=id,merchant_id,code,discount_percent,max_discount,min_order_value,discount_amount,is_active,expires_at,created_at,max_usages_per_user,total_usages_limit,starts_at" +
                    "&is_active=eq.true" +
                    "&or=(merchant_id.eq.${SupabaseRest.encodeQueryValue(ownerId)},merchant_id.is.null)"
            )

            if (!response.isSuccess) {
                throw Exception(SupabaseRest.parseErrorMessage(response.body))
            }

            val list = mutableListOf<Voucher>()
            val array = JSONArray(response.body)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val expiresAtStr = obj.optString("expires_at", "")

                if (expiresAtStr.isNotBlank()) {
                    val isExpired = runCatching {
                        val expiresAt = java.time.Instant.parse(expiresAtStr)
                        expiresAt.isBefore(java.time.Instant.now())
                    }.getOrDefault(false)
                    if (isExpired) continue
                }

                val startsAtStr = obj.optString("starts_at", "")
                if (startsAtStr.isNotBlank()) {
                    val hasNotStarted = runCatching {
                        val startsAt = java.time.Instant.parse(startsAtStr)
                        startsAt.isAfter(java.time.Instant.now())
                    }.getOrDefault(false)
                    if (hasNotStarted) continue
                }

                list.add(
                    Voucher(
                        id = obj.getString("id"),
                        merchantId = obj.optString("merchant_id").takeIf { it.isNotBlank() },
                        code = obj.getString("code"),
                        discountPercent = obj.optInt("discount_percent", 0),
                        maxDiscount = obj.optLong("max_discount", 0L),
                        minOrderValue = obj.optLong("min_order_value", 0L),
                        discountAmount = obj.optLong("discount_amount", 0L),
                        isActive = obj.optBoolean("is_active", true),
                        expiresAt = expiresAtStr.takeIf { it.isNotBlank() },
                        createdAt = obj.optString("created_at").takeIf { it.isNotBlank() },
                        maxUsagesPerUser = if (obj.isNull("max_usages_per_user")) null else obj.getInt("max_usages_per_user"),
                        totalUsagesLimit = if (obj.isNull("total_usages_limit")) null else obj.getInt("total_usages_limit"),
                        startsAt = startsAtStr.takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        }
    }

    private fun fetchStoreOwnerId(storeId: String): String? {
        val response = SupabaseRest.get(
            "/rest/v1/stores?select=owner_id&id=eq.${SupabaseRest.encodeQueryValue(storeId)}"
        )
        if (!response.isSuccess) return null

        val stores = JSONArray(response.body)
        if (stores.length() == 0) return null

        return stores.getJSONObject(0).optString("owner_id").ifBlank { null }
    }

    private fun fetchVoucher(code: String, merchantId: String): VoucherRecord? {
        val response = SupabaseRest.get(
            "/rest/v1/vouchers" +
                "?select=code,discount_percent,max_discount,min_order_value,discount_amount" +
                "&code=eq.${SupabaseRest.encodeQueryValue(code)}" +
                "&or=(merchant_id.eq.${SupabaseRest.encodeQueryValue(merchantId)},merchant_id.is.null)"
        )
        if (!response.isSuccess) return null

        val vouchers = JSONArray(response.body)
        if (vouchers.length() == 0) return null

        val voucher = vouchers.getJSONObject(0)
        return VoucherRecord(
            code = voucher.optString("code"),
            discountPercent = voucher.optInt("discount_percent", 0),
            maxDiscount = voucher.optLong("max_discount", 0L),
            minOrderValue = voucher.optLong("min_order_value", 0L),
            discountAmount = voucher.optLong("discount_amount", 0L)
        )
    }

    private data class VoucherRecord(
        val code: String,
        val discountPercent: Int,
        val maxDiscount: Long,
        val minOrderValue: Long,
        val discountAmount: Long
    )
}
