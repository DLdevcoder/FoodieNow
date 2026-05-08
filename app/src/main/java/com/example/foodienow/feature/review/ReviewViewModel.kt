package com.example.foodienow.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val existingReviewId: String? = null,
    val initialRating: Int = 0,
    val initialComment: String = ""
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val foodId: String = checkNotNull(savedStateHandle["foodId"])

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        checkExistingReview()
    }

    private fun checkExistingReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Sửa ở đây: Truyền cả orderId và foodId để lấy đúng 1 review
                val existingReview = reviewRepository.getReviewByOrderAndFood(orderId, foodId)

                if (existingReview != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            existingReviewId = existingReview.id,
                            initialRating = existingReview.rating,
                            initialComment = existingReview.comment ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Không thể kiểm tra đánh giá cũ") }
            }
        }
    }

    fun submitReview(rating: Int, comment: String) {
        if (rating == 0) {
            _uiState.update { it.copy(error = "Vui lòng chọn số sao đánh giá") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = authRepository.getAuthState().firstOrNull()
                if (user == null) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Không tìm thấy thông tin tài khoản")
                    }
                    return@launch
                }

                val existingId = _uiState.value.existingReviewId
                val success = if (existingId != null) {
                    // Update
                    reviewRepository.updateReview(existingId, rating, comment)
                } else {
                    // Insert: Lúc này truyền foodId chắc chắn sẽ có dữ liệu lưu lên db
                    reviewRepository.submitReview(
                        orderId = orderId,
                        customerId = user.id,
                        foodId = foodId,
                        rating = rating,
                        comment = comment
                    )
                }

                if (success) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Gửi đánh giá thất bại. Vui lòng thử lại.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Có lỗi xảy ra")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}