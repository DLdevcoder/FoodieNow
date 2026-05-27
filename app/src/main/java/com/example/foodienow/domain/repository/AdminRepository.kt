package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.AdminAccountStats
import com.example.foodienow.domain.model.AdminFinancialStats
import com.example.foodienow.domain.model.AdminProfileStats
import com.example.foodienow.domain.model.AdminDetailedFinancialStats
import com.example.foodienow.domain.model.SystemSetting
import com.example.foodienow.domain.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getFinancialStats(): Flow<List<AdminFinancialStats>>

    fun getAccountStats(): Flow<List<AdminAccountStats>>

    suspend fun getAllProfiles(): Result<List<AdminProfileStats>>

    suspend fun updateProfileBalance(userId: String, newBalance: Long): Result<Unit>

    fun getDetailedFinancialStats(): Flow<AdminDetailedFinancialStats>

    suspend fun getSystemSettings(): Result<List<SystemSetting>>

    suspend fun updateSystemSetting(key: String, value: Double): Result<Unit>

    suspend fun getUserTransactions(userId: String): Result<List<WalletTransaction>>
}
