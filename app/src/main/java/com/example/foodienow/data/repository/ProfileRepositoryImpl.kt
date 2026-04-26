package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Profile
import com.example.foodienow.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

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
}

