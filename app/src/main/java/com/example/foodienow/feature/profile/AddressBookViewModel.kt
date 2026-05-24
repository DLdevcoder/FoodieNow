package com.example.foodienow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.data.remote.GoongAddressService
import com.example.foodienow.data.remote.GoongPrediction
import com.example.foodienow.data.repository.MockAddressRepository
import com.example.foodienow.domain.model.Address
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressBookViewModel @Inject constructor(
    private val addressRepository: MockAddressRepository,
    private val goongAddressService: GoongAddressService
) : ViewModel() {

    val addresses: StateFlow<List<Address>> = addressRepository.addresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _predictions = MutableStateFlow<List<GoongPrediction>>(emptyList())
    val predictions: StateFlow<List<GoongPrediction>> = _predictions.asStateFlow()

    private val _selectedLat = MutableStateFlow<Double?>(null)
    val selectedLat: StateFlow<Double?> = _selectedLat.asStateFlow()

    private val _selectedLng = MutableStateFlow<Double?>(null)
    val selectedLng: StateFlow<Double?> = _selectedLng.asStateFlow()

    private val _selectedDetail = MutableStateFlow("")
    val selectedDetail: StateFlow<String> = _selectedDetail.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    fun searchAddress(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _predictions.value = emptyList()
            } else {
                _predictions.value = goongAddressService.getAutocomplete(query)
            }
        }
    }

    fun selectPrediction(prediction: GoongPrediction) {
        _predictions.value = emptyList()
        _selectedDetail.value = prediction.description
        _isResolving.value = true
        viewModelScope.launch {
            val result = goongAddressService.getPlaceDetail(prediction.placeId)
            if (result != null) {
                _selectedLat.value = result.geometry.location.lat
                _selectedLng.value = result.geometry.location.lng
                _selectedDetail.value = result.formattedAddress ?: result.name ?: prediction.description
            }
            _isResolving.value = false
        }
    }

    fun updateLocation(lat: Double, lng: Double, detail: String? = null) {
        _selectedLat.value = lat
        _selectedLng.value = lng
        if (detail != null) {
            _selectedDetail.value = detail
        } else {
            viewModelScope.launch {
                val result = goongAddressService.getReverseGeocode(lat, lng)
                if (result != null) {
                    _selectedDetail.value = result.formattedAddress ?: result.name ?: ""
                }
            }
        }
    }

    fun clearAddForm() {
        _predictions.value = emptyList()
        _selectedLat.value = null
        _selectedLng.value = null
        _selectedDetail.value = ""
        _isResolving.value = false
    }

    fun addAddress(title: String) {
        val detail = _selectedDetail.value
        val lat = _selectedLat.value
        val lng = _selectedLng.value
        if (detail.isNotBlank()) {
            addressRepository.addAddress(title, detail, lat, lng)
            clearAddForm()
        }
    }

    fun setDefault(id: String) {
        addressRepository.setDefault(id)
    }

    fun removeAddress(id: String) {
        addressRepository.removeAddress(id)
    }
}
