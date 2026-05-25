package com.example.foodienow.data.payment

import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.WalletChargeResult
import com.example.foodienow.domain.payment.WalletPaymentGateway
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.random.Random

class RealWalletPaymentGateway @Inject constructor() : WalletPaymentGateway {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // --- DUMMY SANDBOX KEYS (To be replaced by user) ---
    private val vnpTmnCode = "2QXG2YYS"
    private val vnpHashSecret = "971485c2794e43e2a22026857945d8b7"
    private val vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"

    private val momoPartnerCode = "MOMOIQA420180417"
    private val momoAccessKey = "klm05TvNCpe7cgS9"
    private val momoSecretKey = "at67qH6mk8g5i1Peou6QDOTRhCG31T2K"
    private val momoUrl = "https://test-payment.momo.vn/v2/gateway/api/create"

    private val zaloAppId = "2553"
    private val zaloKey1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL"
    private val zaloUrl = "https://sb-openapi.zalopay.vn/v2/create"

    override suspend fun charge(
        provider: WalletProvider,
        amount: Long,
        orderId: String,
        customerId: String
    ): Result<WalletChargeResult> = withContext(Dispatchers.IO) {
        try {
            val returnUrl = "foodienow://payment_result"
            val transactionId = "${orderId}_${System.currentTimeMillis()}"

            val paymentUrl = when (provider) {
                WalletProvider.VNPAY -> createVnPayUrl(amount, transactionId, returnUrl)
                WalletProvider.MOMO -> "foodienow://momo_sandbox?amount=$amount&orderId=$transactionId&returnUrl=$returnUrl"
                WalletProvider.ZALOPAY -> "foodienow://zalopay_sandbox?amount=$amount&orderId=$transactionId&returnUrl=$returnUrl"
                WalletProvider.PAYPAL -> "foodienow://paypal_sandbox?amount=$amount&orderId=$transactionId&returnUrl=$returnUrl"
            }

            if (paymentUrl != null) {
                Result.success(
                    WalletChargeResult(
                        transactionId = transactionId,
                        message = "Success",
                        paymentUrl = paymentUrl
                    )
                )
            } else {
                Result.failure(Exception("Unsupported provider or failed to generate URL"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hmacSHA512(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512")
        mac.init(secretKey)
        val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSHA256(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun createVnPayUrl(amount: Long, orderId: String, returnUrl: String): String {
        val amountVnd = amount * 100 // VNPAY format
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        formatter.timeZone = TimeZone.getTimeZone("GMT+7")
        val createDate = formatter.format(Date())
        val expireDate = formatter.format(Date(System.currentTimeMillis() + 15 * 60 * 1000))

        val vnpParams = mutableMapOf<String, String>()
        vnpParams["vnp_Version"] = "2.1.0"
        vnpParams["vnp_Command"] = "pay"
        vnpParams["vnp_TmnCode"] = vnpTmnCode
        vnpParams["vnp_Amount"] = amountVnd.toString()
        vnpParams["vnp_CurrCode"] = "VND"
        vnpParams["vnp_TxnRef"] = orderId
        vnpParams["vnp_OrderInfo"] = "Thanh toan don hang $orderId"
        vnpParams["vnp_OrderType"] = "other"
        vnpParams["vnp_Locale"] = "vn"
        vnpParams["vnp_ReturnUrl"] = returnUrl
        vnpParams["vnp_IpAddr"] = "127.0.0.1" // Dummy IP
        vnpParams["vnp_CreateDate"] = createDate
        vnpParams["vnp_ExpireDate"] = expireDate

        val fieldNames = vnpParams.keys.toList().sorted()
        val hashData = StringBuilder()
        val query = java.lang.StringBuilder()

        for (fieldName in fieldNames) {
            val fieldValue = vnpParams[fieldName]
            if ((fieldValue != null) && (fieldValue.isNotEmpty())) {
                hashData.append(fieldName)
                hashData.append('=')
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()))
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                query.append('=')
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()))
                if (fieldName != fieldNames.last()) {
                    query.append('&')
                    hashData.append('&')
                }
            }
        }
        val secureHash = hmacSHA512(vnpHashSecret, hashData.toString())
        query.append("&vnp_SecureHash=").append(secureHash)
        return "$vnpUrl?${query.toString()}"
    }

}
