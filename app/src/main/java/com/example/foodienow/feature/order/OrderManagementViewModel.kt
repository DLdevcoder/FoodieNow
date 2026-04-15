package com.example.foodienow.feature.order

import androidx.lifecycle.ViewModel
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val orderRepo: OrderRepository
) : ViewModel() {

    // ===== MERCHANT =====

    // Nhận đơn mới: Lắng nghe realtime từ Database. Nếu có đơn hàng mới (Status = PENDING), báo chuông/hiển thị lên UI.
    fun listenForNewOrders() { }

    // Xử lý đơn: Đổi status từ PENDING -> PREPARING (Đang làm món).
    fun acceptOrder(orderId: String) { }

    // ===== SHIPPER =====

    // Tìm đơn để giao: Hiển thị các đơn hàng đang có status PREPARING mà chưa có ai nhận (shipperId == null).
    fun findAvailableDeliveries() { }

    // Nhận cuốc xe: Gắn ID của shipper vào đơn hàng. Đổi status -> DELIVERING.
    fun takeOrder(orderId: String) {  }

    // Giao thành công: Đổi status -> COMPLETED. Báo cho Customer biết để nhảy sang màn Đánh giá.
    fun completeDelivery(orderId: String) { }
}