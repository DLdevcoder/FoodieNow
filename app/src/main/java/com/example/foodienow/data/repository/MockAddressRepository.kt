package com.example.foodienow.data.repository

import android.util.Log
import com.example.foodienow.domain.model.Address
import com.example.foodienow.domain.model.AddressInsert
import com.example.foodienow.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.gotrue.auth

@Singleton
class MockAddressRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository
) {
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: Flow<List<Address>> = _addresses.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            authRepository.getAuthState().collectLatest { user ->
                if (user == null) {
                    try { supabaseClient.auth.clearSession() } catch (e: Exception) {}
                    _addresses.value = emptyList()
                    return@collectLatest
                }
                try {
                    supabaseClient.auth.importAuthToken(user.token, user.refreshToken)
                } catch (e: Exception) {
                    Log.e("MockAddressRepository", "Error importing token", e)
                }
                fetchAddresses(user.id)
            }
        }
    }

    private suspend fun fetchAddresses(userId: String) {
        try {
            val list = supabaseClient.postgrest["addresses"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Address>()
            _addresses.value = list
        } catch (e: Exception) {
            Log.e("MockAddressRepository", "Error fetching addresses", e)
        }
    }

    fun addAddress(title: String, detail: String, latitude: Double?, longitude: Double?) {
        repositoryScope.launch {
            try {
                val user = authRepository.getAuthState().first() ?: return@launch
                val currentList = _addresses.value
                val isDefault = currentList.isEmpty() || currentList.none { it.isDefault }
                val insert = AddressInsert(
                    userId = user.id,
                    title = title,
                    detail = detail,
                    isDefault = isDefault,
                    latitude = latitude,
                    longitude = longitude
                )
                supabaseClient.postgrest["addresses"].insert(insert)
                fetchAddresses(user.id)
            } catch (e: Exception) {
                Log.e("MockAddressRepository", "Error adding address", e)
            }
        }
    }

    fun setDefault(id: String) {
        repositoryScope.launch {
            try {
                val user = authRepository.getAuthState().first() ?: return@launch

                supabaseClient.postgrest["addresses"].update({
                    set("is_default", false)
                }) {
                    filter {
                        eq("user_id", user.id)
                    }
                }

                supabaseClient.postgrest["addresses"].update({
                    set("is_default", true)
                }) {
                    filter {
                        eq("id", id)
                    }
                }

                fetchAddresses(user.id)
            } catch (e: Exception) {
                Log.e("MockAddressRepository", "Error setting default address", e)
            }
        }
    }

    fun removeAddress(id: String) {
        repositoryScope.launch {
            try {
                val user = authRepository.getAuthState().first() ?: return@launch
                supabaseClient.postgrest["addresses"].delete {
                    filter {
                        eq("id", id)
                    }
                }
                fetchAddresses(user.id)
            } catch (e: Exception) {
                Log.e("MockAddressRepository", "Error removing address", e)
            }
        }
    }
}
