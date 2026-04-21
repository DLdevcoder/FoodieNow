package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.OrderRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].insert(order)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMerchantOrders(merchantId: String): Flow<List<Order>> = flow {
        val response = supabaseClient.postgrest["orders"]
            .select {
                filter {
                    eq("merchantId", merchantId)
                }
            }
            .decodeList<Order>()
        emit(response)
    }

    override fun getAvailableDeliveries(): Flow<List<Order>> = flow {
        val response = supabaseClient.postgrest["orders"]
            .select {
                filter {
                    eq("status", OrderStatus.PREPARING)
                    // In a real app, you might also check if shipperId is null
                }
            }
            .decodeList<Order>()
        emit(response)
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("status", newStatus)
                }
            ) {
                filter {
                    eq("id", orderId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
