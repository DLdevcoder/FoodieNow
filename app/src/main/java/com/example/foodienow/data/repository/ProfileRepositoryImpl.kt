package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Profile
import com.example.foodienow.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

    private companion object {
        const val AVATAR_BUCKET = "profile_avatars"
    }

    override fun getProfile(userId: String): Flow<Profile?> = flow {
        val profile = supabaseClient.postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<Profile>()
            .firstOrNull()
        emit(profile)
    }

    override suspend fun upsertProfile(profile: Profile): Result<Profile> {
        return try {
            try {
                supabaseClient.postgrest["profiles"].insert(profile)
            } catch (insertError: Exception) {
                // Fallback update for existing profile rows.
                supabaseClient.postgrest["profiles"].update(profile) {
                    filter {
                        eq("id", profile.id)
                    }
                }
            }

            val savedProfile = supabaseClient.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", profile.id)
                    }
                }
                .decodeList<Profile>()
                .firstOrNull()
                ?: profile

            Result.success(savedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(profile: Profile, imageBytes: ByteArray): Result<Profile> {
        return try {
            val bucket = supabaseClient.storage[AVATAR_BUCKET]
            val oldAvatarPath = profile.avatarUrl.toAvatarStoragePath()
            val filePath = "${profile.id}/avatar_${System.currentTimeMillis()}.jpg"
            bucket.upload(filePath, imageBytes)

            val avatarUrl = bucket.publicUrl(filePath)
            updateAvatarUrl(profile.id, avatarUrl)
                .onSuccess {
                    if (oldAvatarPath != null && oldAvatarPath != filePath) {
                        runCatching { bucket.delete(oldAvatarPath) }
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAvatar(profile: Profile): Result<Profile> {
        return try {
            val avatarPath = profile.avatarUrl.toAvatarStoragePath()
            val bucket = supabaseClient.storage[AVATAR_BUCKET]

            val result = updateAvatarUrl(profile.id, null)
            if (result.isSuccess && avatarPath != null) {
                runCatching { bucket.delete(avatarPath) }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["profiles"].update(
                {
                    set("fcm_token", token)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateAvatarUrl(userId: String, avatarUrl: String?): Result<Profile> {
        return try {
            supabaseClient.postgrest["profiles"].update(
                {
                    set("avatar_url", avatarUrl)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }

            val savedProfile = supabaseClient.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<Profile>()
                .first()

            Result.success(savedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun String?.toAvatarStoragePath(): String? {
        if (isNullOrBlank()) return null
        val marker = "/storage/v1/object/public/$AVATAR_BUCKET/"
        val markerIndex = indexOf(marker)
        if (markerIndex == -1) return null
        return substring(markerIndex + marker.length)
            .substringBefore("?")
            .takeIf { it.isNotBlank() }
    }
}

