package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.domain.model.Address
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AddressBookViewModel @Inject constructor(
    private val addressRepository: MockAddressRepository
) : ViewModel() {

    val addresses: StateFlow<List<Address>> = addressRepository.addresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAddress(title: String, detail: String) {
        addressRepository.addAddress(title, detail)
    }

    fun setDefault(id: String) {
        addressRepository.setDefault(id)
    }

    fun removeAddress(id: String) {
        addressRepository.removeAddress(id)
    }
}
