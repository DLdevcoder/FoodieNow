package com.example.foodienow.domain.repository

import com.example.foodienow.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(userId: String): Flow<Profile?>

    suspend fun upsertProfile(profile: Profile): Result<Profile>

    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
}

