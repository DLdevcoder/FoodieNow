package com.example.foodienow.feature.shipper_tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipperTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Lấy orderId từ tham số điều hướng
    val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    init {
        // Giả sử bạn có hàm getOrderById, nếu chưa có, hãy thêm vào OrderRepository
        loadOrder()
    }

    private fun loadOrder() {
        // TODO: Chỗ này bạn gọi hàm lấy chi tiết Order từ Supabase theo orderId
        // viewModelScope.launch {
        //    _currentOrder.value = orderRepository.getOrderById(orderId)
        // }
    }

    // Hàm này sẽ được gọi liên tục khi GPS của Shipper thay đổi
    fun updateShipperLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            orderRepository.updateShipperLocation(orderId, lat, lng)
            // Cập nhật lại state cục bộ để xe di chuyển trên bản đồ
            _currentOrder.update {
                it?.copy(shipperLat = lat, shipperLng = lng)
            }
        }
    }
}