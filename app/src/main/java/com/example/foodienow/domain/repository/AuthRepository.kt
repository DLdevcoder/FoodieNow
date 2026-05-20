package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Input: email, password
    suspend fun login(email: String, pass: String): Result<User>

    // Input: thông tin đăng ký
    suspend fun register(email: String, pass: String, role: UserRole): Result<User>

    // Gửi lại email xác thực tài khoản
    suspend fun resendVerificationEmail(email: String): Result<Unit>

    // Gửi email đặt lại mật khẩu
    suspend fun forgotPassword(email: String): Result<Unit>

    // Đăng xuất và xóa trạng thái đăng nhập hiện tại
    suspend fun logout(): Result<Unit>

    // Output: Flow theo dõi trạng thái đăng nhập để tự động văng ra màn hình Login nếu token hết hạn
    fun getAuthState(): Flow<User?>

    // Nạp tiền hoặc trừ tiền
    suspend fun updateBalance(amount: Long): Result<User>

    // Cộng hoặc trừ điểm thưởng
    suspend fun updateRewardPoints(points: Int): Result<User>

    // Cap nhat so du va diem trong phien sau khi server da xu ly giao dich
    suspend fun updateSessionFinancials(balance: Long, rewardPoints: Int): Result<User>

    // Đổi mật khẩu
    suspend fun changePassword(newPass: String): Result<Unit>
    suspend fun resolveStoredSession(): User?
}
