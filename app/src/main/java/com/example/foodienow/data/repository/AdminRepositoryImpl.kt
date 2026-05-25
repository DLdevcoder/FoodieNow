package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.AdminAccountStats
import com.example.foodienow.domain.model.AdminFinancialStats
import com.example.foodienow.domain.model.AdminProfileStats
import com.example.foodienow.domain.model.AdminDetailedFinancialStats
import com.example.foodienow.domain.model.SystemSetting
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.repository.AdminRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AdminRepository {

    override fun getFinancialStats(): Flow<List<AdminFinancialStats>> = flow {
        val list = supabaseClient.postgrest["admin_financial_dashboard"]
            .select()
            .decodeList<AdminFinancialStats>()
        emit(list)
    }.flowOn(Dispatchers.IO)

    override fun getAccountStats(): Flow<List<AdminAccountStats>> = flow {
        val list = supabaseClient.postgrest["admin_account_stats"]
            .select()
            .decodeList<AdminAccountStats>()
        emit(list)
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllProfiles(): Result<List<AdminProfileStats>> = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.postgrest["profiles"]
                .select()
                .decodeList<AdminProfileStats>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfileBalance(userId: String, newBalance: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["profiles"].update(
                mapOf("balance" to newBalance)
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDetailedFinancialStats(): Flow<AdminDetailedFinancialStats> = flow {
        val stats = supabaseClient.postgrest["admin_detailed_financial_stats"]
            .select()
            .decodeSingle<AdminDetailedFinancialStats>()
        emit(stats)
    }.flowOn(Dispatchers.IO)

    override suspend fun getSystemSettings(): Result<List<SystemSetting>> = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.postgrest["system_settings"]
                .select()
                .decodeList<SystemSetting>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSystemSetting(key: String, value: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["system_settings"].update(
                mapOf("value" to value)
            ) {
                filter {
                    eq("key", key)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserTransactions(userId: String): Result<List<WalletTransaction>> = withContext(Dispatchers.IO) {
        try {
            val list = supabaseClient.postgrest["wallet_transactions"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<WalletTransaction>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
