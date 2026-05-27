package com.example.foodienow.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.model.AdminAccountStats
import com.example.foodienow.domain.model.AdminFinancialStats
import com.example.foodienow.domain.model.AdminProfileStats
import com.example.foodienow.domain.model.AdminDetailedFinancialStats
import com.example.foodienow.domain.model.SystemSetting
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchCriteria {
    ALL, NAME, EMAIL, ID
}

data class AdminUiState(
    val isFinancialLoading: Boolean = false,
    val financialStats: List<AdminFinancialStats> = emptyList(),
    val isAccountLoading: Boolean = false,
    val accountStats: List<AdminAccountStats> = emptyList(),
    val allProfiles: List<AdminProfileStats> = emptyList(),
    val filteredProfiles: List<AdminProfileStats> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: UserRole? = null,
    val searchCriteria: SearchCriteria = SearchCriteria.ALL,
    val isActionProcessing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val detailedFinancialStats: AdminDetailedFinancialStats = AdminDetailedFinancialStats(),
    val systemSettings: List<SystemSetting> = emptyList(),
    val userTransactions: List<WalletTransaction> = emptyList(),
    val isTransactionsLoading: Boolean = false,
    val isDetailedFinancialLoading: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadFinancialStats()
        loadAccountStats()
        loadProfiles()
        loadDetailedFinancialStats()
        loadSystemSettings()
    }

    fun loadFinancialStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFinancialLoading = true, errorMessage = null) }
            try {
                adminRepository.getFinancialStats().collect { stats ->
                    _uiState.update { it.copy(isFinancialLoading = false, financialStats = stats) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isFinancialLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadAccountStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccountLoading = true, errorMessage = null) }
            try {
                adminRepository.getAccountStats().collect { stats ->
                    _uiState.update { it.copy(isAccountLoading = false, accountStats = stats) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAccountLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccountLoading = true, errorMessage = null) }
            adminRepository.getAllProfiles()
                .onSuccess { profiles ->
                    _uiState.update { state ->
                        state.copy(
                            isAccountLoading = false,
                            allProfiles = profiles
                        )
                    }
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAccountLoading = false,
                            errorMessage = "Không thể tải danh sách tài khoản: ${error.message}"
                        )
                    }
                }
        }
    }

    fun loadDetailedFinancialStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetailedFinancialLoading = true) }
            try {
                adminRepository.getDetailedFinancialStats().collect { stats ->
                    _uiState.update { it.copy(isDetailedFinancialLoading = false, detailedFinancialStats = stats) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDetailedFinancialLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadSystemSettings() {
        viewModelScope.launch {
            adminRepository.getSystemSettings()
                .onSuccess { settings ->
                    _uiState.update { it.copy(systemSettings = settings) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = "Không thể tải cấu hình: ${error.message}") }
                }
        }
    }

    fun updateSystemSetting(key: String, value: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionProcessing = true, errorMessage = null, successMessage = null) }
            adminRepository.updateSystemSetting(key, value)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionProcessing = false,
                            successMessage = "Cập nhật cấu hình thành công."
                        )
                    }
                    loadSystemSettings()
                    loadDetailedFinancialStats()
                    loadFinancialStats()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isActionProcessing = false,
                            errorMessage = "Cập nhật cấu hình thất bại: ${error.message}"
                        )
                    }
                }
        }
    }

    fun loadUserTransactions(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTransactionsLoading = true, userTransactions = emptyList()) }
            adminRepository.getUserTransactions(userId)
                .onSuccess { txs ->
                    _uiState.update { it.copy(isTransactionsLoading = false, userTransactions = txs) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            errorMessage = "Không thể tải lịch sử giao dịch: ${error.message}"
                        )
                    }
                }
        }
    }

    fun clearUserTransactions() {
        _uiState.update { it.copy(userTransactions = emptyList()) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateRoleFilter(role: UserRole?) {
        _uiState.update { it.copy(selectedRoleFilter = role) }
        applyFilters()
    }

    fun updateSearchCriteria(criteria: SearchCriteria) {
        _uiState.update { it.copy(searchCriteria = criteria) }
        applyFilters()
    }

    fun updateUserBalance(userId: String, newBalance: Long) {
        if (newBalance < 0) {
            _uiState.update { it.copy(errorMessage = "Số dư không được là số âm.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionProcessing = true, errorMessage = null, successMessage = null) }
            adminRepository.updateProfileBalance(userId, newBalance)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionProcessing = false,
                            successMessage = "Cập nhật số dư thành công."
                        )
                    }
                    loadProfiles()
                    loadAccountStats()
                    loadDetailedFinancialStats()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isActionProcessing = false,
                            errorMessage = "Cập nhật số dư thất bại: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun applyFilters() {
        val current = _uiState.value
        var list = current.allProfiles

        if (current.searchQuery.isNotBlank()) {
            val q = current.searchQuery.trim().lowercase()
            list = list.filter {
                when (current.searchCriteria) {
                    SearchCriteria.ALL -> it.fullName.lowercase().contains(q) || it.email.lowercase().contains(q) || it.id.lowercase().contains(q)
                    SearchCriteria.NAME -> it.fullName.lowercase().contains(q)
                    SearchCriteria.EMAIL -> it.email.lowercase().contains(q)
                    SearchCriteria.ID -> it.id.lowercase().contains(q)
                }
            }
        }

        if (current.selectedRoleFilter != null) {
            list = list.filter { it.role == current.selectedRoleFilter }
        }

        _uiState.update { it.copy(filteredProfiles = list) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
