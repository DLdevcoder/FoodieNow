package com.example.foodienow.feature.auth

import androidx.lifecycle.ViewModel
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

//@HiltViewModel
//class AuthViewModel @Inject constructor(
//    private val authRepository: AuthRepository // Kết nối API
//) : ViewModel() {
//
//    // Trạng thái màn hình: Đang load, Lỗi, hoặc Thành công
//    val uiState = MutableStateFlow(AuthUiState())
//
//    // 1. Hàm Đăng nhập
//    // - Input: Email, Mật khẩu
//    // - Nhiệm vụ: Gọi API kiểm tra. Nếu đúng -> Lưu Token và UserRole vào máy (DataStore/SharePref) -> Báo UI chuyển trang.
//    fun login(email: String, pass: String) { }
//
//    // 2. Hàm Đăng ký
//    // - Input: Tên, Email, Mật khẩu, VAI TRÒ (Customer, Merchant, hay Shipper)
//    // - Nhiệm vụ: Tạo tài khoản mới trên Database.
//    fun register(name: String, email: String, pass: String, role: UserRole) { ... }
//
//    // 3. Hàm Đăng xuất
//    // - Nhiệm vụ: Xóa sạch Token, UserRole khỏi máy -> Đẩy user về lại màn hình Login.
//    fun logout() { }
//}