package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Input: email, password
    suspend fun login(email: String, pass: String): Result<User>

    // Input: thông tin đăng ký
    suspend fun register(email: String, pass: String, role: UserRole): Result<User>

    // Output: Flow theo dõi trạng thái đăng nhập để tự động văng ra màn hình Login nếu token hết hạn
    fun getAuthState(): Flow<User?>
}