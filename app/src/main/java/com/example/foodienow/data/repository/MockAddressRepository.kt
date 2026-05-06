package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAddressRepository @Inject constructor() {
    private val defaultAddresses = listOf(
        Address(
            title = "Nhà riêng",
            detail = "Số 123 Đường Nguyễn Trãi, Quận Thanh Xuân, Hà Nội",
            isDefault = true
        ),
        Address(
            title = "Công ty",
            detail = "Tòa nhà Keangnam, Phạm Hùng, Nam Từ Liêm, Hà Nội",
            isDefault = false
        )
    )

    private val _addresses = MutableStateFlow(defaultAddresses)
    val addresses: Flow<List<Address>> = _addresses.asStateFlow()

    fun addAddress(title: String, detail: String) {
        val newAddress = Address(title = title, detail = detail, isDefault = _addresses.value.isEmpty())
        _addresses.update { current ->
            current + newAddress
        }
    }

    fun setDefault(id: String) {
        _addresses.update { current ->
            current.map {
                it.copy(isDefault = it.id == id)
            }
        }
    }
    
    fun removeAddress(id: String) {
        _addresses.update { current ->
            current.filter { it.id != id }
        }
    }
}
