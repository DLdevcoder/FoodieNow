package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.repository.OrderRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<Order> {
        return try {
            val insertedOrder = supabaseClient.postgrest["orders"]
                .insert(order) {
                    select()
                }
                .decodeSingle<Order>()
            Result.success(insertedOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMerchantOrders(merchantId: String): Flow<List<Order>> = flow {
        val response = supabaseClient.postgrest["orders"]
            .select {
                filter {
                    eq("merchant_id", merchantId)
                }
            }
            .decodeList<Order>()
        emit(response)
    }

    override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = channelFlow {
        val fetchOrders = suspend {
            supabaseClient.postgrest["orders"]
                .select { filter { eq("customer_id", customerId) } }
                .decodeList<Order>()
        }

        // Emit initial data
        send(fetchOrders())

        // Setup realtime channel
        val channelName = "orders_customer_$customerId"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
        }

        launch {
            changes.collect {
                // Whenever there's an insert/update/delete, refetch
                send(fetchOrders())
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    override fun getAvailableDeliveries(): Flow<List<Order>> = channelFlow {
        val fetchOrders = suspend {
            supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("status", OrderStatus.PREPARING.name)
                        // Local fallback filter is used instead to avoid syntax issues
                    }
                }
                .decodeList<Order>()
                .filter { it.shipperId == null } // Fallback local filter just in case
        }

        send(fetchOrders())

        val channelName = "orders_shipper_available"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
        }

        launch {
            changes.collect {
                send(fetchOrders())
            }
        }

        channel.subscribe()
        awaitClose {
            launch { supabaseClient.realtime.removeChannel(channel) }
        }
    }

    override fun getShipperActiveOrder(shipperId: String): Flow<Order?> = channelFlow {
        val fetchActive = suspend {
            supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("shipper_id", shipperId)
                        eq("status", OrderStatus.DELIVERING.name)
                    }
                }
                .decodeList<Order>()
                .firstOrNull()
        }

        send(fetchActive())

        val channelName = "orders_shipper_active_$shipperId"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
        }

        launch {
            changes.collect {
                send(fetchActive())
            }
        }

        channel.subscribe()
        awaitClose {
            launch { supabaseClient.realtime.removeChannel(channel) }
        }
    }

    override suspend fun acceptOrder(orderId: String, shipperId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("shipper_id", shipperId)
                    set("status", OrderStatus.DELIVERING.name)
                }
            ) {
                filter { eq("id", orderId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("status", newStatus.name)
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
