package com.example.foodienow.data.repository

import com.example.foodienow.data.remote.SupabaseRest
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.WalletProvider
import com.example.foodienow.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class PaymentSettingsState(
    val defaultMethod: PaymentMethod = PaymentMethod.COD,
    val defaultProvider: WalletProvider = WalletProvider.ZALOPAY,
    val isLoaded: Boolean = false
)

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
            val response = SupabaseRest.get(
                path = "/rest/v1/payment_settings" +
                    "?select=default_method,default_provider" +
                    "&user_id=eq.${SupabaseRest.encodeQueryValue(user.id)}",
                accessToken = user.token
            )

            if (!response.isSuccess) {
                throw Exception(SupabaseRest.parseErrorMessage(response.body))
            }

            val records = JSONArray(response.body)
            val loaded = if (records.length() == 0) {
                PaymentSettingsState(isLoaded = true)
            } else {
                records.getJSONObject(0).toPaymentSettingsState()
            }

            _settings.value = loaded
            loaded
        }
    }

    suspend fun updateSettings(
        method: PaymentMethod,
        provider: WalletProvider
    ): Result<PaymentSettingsState> = withContext(Dispatchers.IO) {
        val user = authRepository.getAuthState().firstOrNull()
            ?: return@withContext Result.failure(Exception("No user logged in"))

        runCatching {
            val body = JSONObject()
                .put("user_id", user.id)
                .put("default_method", method.name)
                .put("default_provider", provider.name)

            val response = SupabaseRest.post(
                path = "/rest/v1/payment_settings?on_conflict=user_id",
                body = body,
                accessToken = user.token,
                prefer = "resolution=merge-duplicates,return=representation"
            )

            if (!response.isSuccess) {
                throw Exception(SupabaseRest.parseErrorMessage(response.body))
            }

            val records = JSONArray(response.body)
            val updated = if (records.length() == 0) {
                PaymentSettingsState(method, provider, isLoaded = true)
            } else {
                records.getJSONObject(0).toPaymentSettingsState()
            }

            _settings.value = updated
            updated
        }
    }

    private fun JSONObject.toPaymentSettingsState(): PaymentSettingsState {
        val method = runCatching {
            PaymentMethod.valueOf(optString("default_method").uppercase())
        }.getOrDefault(PaymentMethod.COD)

        val provider = runCatching {
            WalletProvider.valueOf(optString("default_provider").uppercase())
        }.getOrDefault(WalletProvider.ZALOPAY)

        return PaymentSettingsState(
            defaultMethod = method,
            defaultProvider = provider,
            isLoaded = true
        )
    }
}
