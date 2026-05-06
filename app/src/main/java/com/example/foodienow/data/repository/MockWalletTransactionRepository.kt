package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.WalletTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockWalletTransactionRepository @Inject constructor() {
    private val transactionsFlow = MutableStateFlow<List<WalletTransaction>>(emptyList())

    fun getTransactions(): Flow<List<WalletTransaction>> {
        return transactionsFlow.map { it.sortedByDescending { tx -> tx.createdAt } }
    }

    fun addTransaction(transaction: WalletTransaction) {
        transactionsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()
            mutableList.add(transaction)
            mutableList
        }
    }
}
