package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderItemUiModel
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

    // Shipper xem đơn đang giao
    fun getShipperActiveOrder(shipperId: String): Flow<Order?>

    // Shipper nhận đơn
    suspend fun acceptOrder(orderId: String, shipperId: String): Result<Unit>

    // cập nhật trạng thái
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit>

    // Shipper xem đơn hoàn thành
    fun getShipperCompletedOrders(shipperId: String): Flow<List<Order>>

    suspend fun getOrderItemsByOrderId(orderId: String): List<OrderItemUiModel>

    suspend fun updateShipperLocation(orderId: String, lat: Double, lng: Double): Result<Unit>
}