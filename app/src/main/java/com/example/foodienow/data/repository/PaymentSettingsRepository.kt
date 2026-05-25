package com.example.foodienow.data.repository

import com.example.foodienow.data.remote.SupabaseRest
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.payment.PaymentMethodCatalog
import com.example.foodienow.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class PaymentMethodInfo(
    val optionId: String,
    val displayName: String,
    val details: String
)

data class PaymentSettingsState(
    val defaultMethod: PaymentMethod = PaymentMethod.COD,
    val defaultProvider: WalletProvider = WalletProvider.ZALOPAY,
    val methodInfos: Map<String, PaymentMethodInfo> = emptyMap(),
    val isLoaded: Boolean = false
) {
    val configuredOptionIds: Set<String>
        get() = methodInfos.keys

    fun isOptionAvailable(optionId: String): Boolean {
        return PaymentMethodCatalog.isOptionAvailable(optionId, configuredOptionIds)
    }
}

@Singleton
class PaymentSettingsRepository @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val _settings = MutableStateFlow(PaymentSettingsState())
    val settings: StateFlow<PaymentSettingsState> = _settings.asStateFlow()

    suspend fun refreshSettings(): Result<PaymentSettingsState> = withContext(Dispatchers.IO) {
        val user = authRepository.getAuthState().firstOrNull()
            ?: return@withContext Result.failure(Exception("No user logged in"))

        runCatching {
            val response = fetchSettings(user.id, user.token, includeMethodInfos = true)

            if (!response.isSuccess) {
                val legacyResponse = fetchSettings(user.id, user.token, includeMethodInfos = false)
                if (!legacyResponse.isSuccess) {
                    throw Exception(SupabaseRest.parseErrorMessage(response.body))
                }
                return@runCatching readSettingsResponse(legacyResponse.body, user.role)
                    .also { _settings.value = it }
            }

            readSettingsResponse(response.body, user.role).also { _settings.value = it }
        }
    }

    suspend fun updateDefaultMethod(optionId: String): Result<PaymentSettingsState> =
        withContext(Dispatchers.IO) {
            val user = authRepository.getAuthState().firstOrNull()
                ?: return@withContext Result.failure(Exception("No user logged in"))

            runCatching {
                if (user.role == com.example.foodienow.domain.model.UserRole.ADMIN && optionId == PaymentMethodCatalog.COD_ID) {
                    throw IllegalArgumentException("Admin không thể sử dụng phương thức thanh toán bằng tiền mặt (COD).")
                }

                val option = PaymentMethodCatalog.optionFor(optionId)
                    ?: throw IllegalArgumentException("Phuong thuc thanh toan khong hop le.")

                val current = _settings.value
                if (!current.isOptionAvailable(optionId)) {
                    throw IllegalStateException("Vui long cai dat thong tin truoc khi dat lam mac dinh.")
                }

                upsertSettings(
                    userId = user.id,
                    accessToken = user.token,
                    method = option.method,
                    provider = option.provider ?: current.defaultProvider,
                    methodInfos = current.methodInfos,
                    role = user.role
                )
            }
        }

    suspend fun updateSettings(
        method: PaymentMethod,
        provider: WalletProvider
    ): Result<PaymentSettingsState> {
        return updateDefaultMethod(PaymentMethodCatalog.optionIdFor(method, provider))
    }

    suspend fun savePaymentMethodInfo(
        optionId: String,
        displayName: String,
        details: String
    ): Result<PaymentSettingsState> = withContext(Dispatchers.IO) {
        val user = authRepository.getAuthState().firstOrNull()
            ?: return@withContext Result.failure(Exception("No user logged in"))

        runCatching {
            val option = PaymentMethodCatalog.optionFor(optionId)
                ?: throw IllegalArgumentException("Phuong thuc thanh toan khong hop le.")

            if (!option.requiresSetup) {
                throw IllegalArgumentException("Phuong thuc nay khong can cai dat thong tin.")
            }

            val normalizedDetails = details.trim()
            if (normalizedDetails.isBlank()) {
                throw IllegalArgumentException("Thong tin thanh toan khong duoc de trong.")
            }

            val current = _settings.value
            val info = PaymentMethodInfo(
                optionId = optionId,
                displayName = displayName.trim().ifBlank { optionId },
                details = normalizedDetails
            )

            upsertSettings(
                userId = user.id,
                accessToken = user.token,
                method = current.defaultMethod,
                provider = current.defaultProvider,
                methodInfos = current.methodInfos + (optionId to info),
                role = user.role
            )
        }
    }

    suspend fun removePaymentMethodInfo(optionId: String): Result<PaymentSettingsState> =
        withContext(Dispatchers.IO) {
            val user = authRepository.getAuthState().firstOrNull()
                ?: return@withContext Result.failure(Exception("No user logged in"))

            runCatching {
                val option = PaymentMethodCatalog.optionFor(optionId)
                    ?: throw IllegalArgumentException("Phuong thuc thanh toan khong hop le.")

                if (!option.requiresSetup) {
                    throw IllegalArgumentException("Phuong thuc nay khong co thong tin de go.")
                }

                val current = _settings.value
                val updatedInfos = current.methodInfos - optionId
                val currentDefaultId = PaymentMethodCatalog.optionIdFor(
                    method = current.defaultMethod,
                    provider = current.defaultProvider
                )
                val nextMethod = if (currentDefaultId == optionId) {
                    if (user.role == com.example.foodienow.domain.model.UserRole.ADMIN) {
                        PaymentMethod.FOODIE_PAY
                    } else {
                        PaymentMethod.COD
                    }
                } else {
                    current.defaultMethod
                }
                val nextProvider = current.defaultProvider

                upsertSettings(
                    userId = user.id,
                    accessToken = user.token,
                    method = nextMethod,
                    provider = nextProvider,
                    methodInfos = updatedInfos,
                    role = user.role
                )
            }
        }

    private fun fetchSettings(
        userId: String,
        accessToken: String,
        includeMethodInfos: Boolean
    ) = SupabaseRest.get(
        path = "/rest/v1/payment_settings" +
            "?select=${if (includeMethodInfos) "default_method,default_provider,method_infos" else "default_method,default_provider"}" +
            "&user_id=eq.${SupabaseRest.encodeQueryValue(userId)}",
        accessToken = accessToken
    )

    private fun readSettingsResponse(body: String, role: com.example.foodienow.domain.model.UserRole): PaymentSettingsState {
        val records = JSONArray(body)
        return if (records.length() == 0) {
            PaymentSettingsState(isLoaded = true).withValidDefault(role)
        } else {
            records.getJSONObject(0).toPaymentSettingsState(role)
        }
    }

    private fun upsertSettings(
        userId: String,
        accessToken: String,
        method: PaymentMethod,
        provider: WalletProvider,
        methodInfos: Map<String, PaymentMethodInfo>,
        role: com.example.foodienow.domain.model.UserRole?
    ): PaymentSettingsState {
        val body = JSONObject()
            .put("user_id", userId)
            .put("default_method", method.name)
            .put("default_provider", provider.name)
            .put("method_infos", methodInfos.toJsonObject())
            .put("updated_at", Instant.now().toString())

        val response = SupabaseRest.post(
            path = "/rest/v1/payment_settings?on_conflict=user_id",
            body = body,
            accessToken = accessToken,
            prefer = "resolution=merge-duplicates,return=representation"
        )

        if (!response.isSuccess) {
            throw Exception(SupabaseRest.parseErrorMessage(response.body))
        }

        val records = JSONArray(response.body)
        val updated = if (records.length() == 0) {
            PaymentSettingsState(
                defaultMethod = method,
                defaultProvider = provider,
                methodInfos = methodInfos,
                isLoaded = true
            ).withValidDefault(role)
        } else {
            records.getJSONObject(0).toPaymentSettingsState(role)
        }

        _settings.value = updated
        return updated
    }

    private fun JSONObject.toPaymentSettingsState(role: com.example.foodienow.domain.model.UserRole?): PaymentSettingsState {
        val method = runCatching {
            PaymentMethod.valueOf(optString("default_method").uppercase())
        }.getOrDefault(PaymentMethod.COD)

        val provider = runCatching {
            WalletProvider.valueOf(optString("default_provider").uppercase())
        }.getOrDefault(WalletProvider.ZALOPAY)

        return PaymentSettingsState(
            defaultMethod = method,
            defaultProvider = provider,
            methodInfos = optJSONObject("method_infos").toPaymentMethodInfoMap(),
            isLoaded = true
        ).withValidDefault(role)
    }

    private fun PaymentSettingsState.withValidDefault(role: com.example.foodienow.domain.model.UserRole?): PaymentSettingsState {
        val defaultOptionId = PaymentMethodCatalog.optionIdFor(defaultMethod, defaultProvider)
        return if (role == com.example.foodienow.domain.model.UserRole.ADMIN && defaultMethod == PaymentMethod.COD) {
            copy(defaultMethod = PaymentMethod.FOODIE_PAY)
        } else if (isOptionAvailable(defaultOptionId)) {
            this
        } else {
            copy(defaultMethod = PaymentMethod.COD)
        }
    }

    private fun Map<String, PaymentMethodInfo>.toJsonObject(): JSONObject {
        return JSONObject().apply {
            values.forEach { info ->
                put(
                    info.optionId,
                    JSONObject()
                        .put("display_name", info.displayName)
                        .put("details", info.details)
                )
            }
        }
    }

    private fun JSONObject?.toPaymentMethodInfoMap(): Map<String, PaymentMethodInfo> {
        if (this == null) return emptyMap()

        val parsed = mutableMapOf<String, PaymentMethodInfo>()
        val keys = keys()
        while (keys.hasNext()) {
            val optionId = keys.next()
            val option = PaymentMethodCatalog.optionFor(optionId) ?: continue
            if (!option.requiresSetup) continue

            val value = optJSONObject(optionId) ?: continue
            val details = value.optString("details").trim()
            if (details.isBlank()) continue

            parsed[optionId] = PaymentMethodInfo(
                optionId = optionId,
                displayName = value.optString("display_name").trim().ifBlank { optionId },
                details = details
            )
        }

        return parsed
    }
}
