package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.WalletTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.channelFlow

@Singleton
class MockWalletTransactionRepository {
    private val supabaseClient: SupabaseClient?
    private val transactionsFlow = MutableStateFlow<List<WalletTransaction>>(emptyList())

    @Inject
    constructor(supabaseClient: SupabaseClient) {
        this.supabaseClient = supabaseClient
    }

    constructor() {
        this.supabaseClient = null
    }

    fun getTransactions(userId: String = ""): Flow<List<WalletTransaction>> = channelFlow {
        if (supabaseClient != null && userId.isNotBlank()) {
            val remoteList = try {
                supabaseClient.postgrest["wallet_transactions"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<WalletTransaction>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            send(remoteList.sortedByDescending { it.createdAt })
        } else {
            transactionsFlow.collect { list ->
                send(list.sortedByDescending { it.createdAt })
            }
        }
    }

    fun addTransaction(transaction: WalletTransaction) {
        transactionsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()
            mutableList.add(transaction)
            mutableList
        }
    }

    suspend fun addTransaction(userId: String, transaction: WalletTransaction): Result<Unit> {
        addTransaction(transaction)
        if (supabaseClient != null && userId.isNotBlank()) {
            return try {
                supabaseClient.postgrest["wallet_transactions"].insert(
                    mapOf(
                        "id" to transaction.id,
                        "user_id" to userId,
                        "type" to transaction.type.name,
                        "amount" to transaction.amount,
                        "description" to transaction.description,
                        "created_at" to transaction.createdAt
                    )
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        return Result.success(Unit)
    }
}
