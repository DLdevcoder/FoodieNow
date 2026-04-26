package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    // khi khách bấm "Thanh toán"
    suspend fun createOrder(order: Order): Result<Order>

    // Merchant lấy đơn mới
    fun getMerchantOrders(merchantId: String): Flow<List<Order>>

    // Customer xem đơn cua minh
    fun getOrdersByCustomer(customerId: String): Flow<List<Order>>

    // Shipper tìm đơn cần giao
    fun getAvailableDeliveries(): Flow<List<Order>>

    // cập nhật trạng thái
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit>
}