package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.MerchantRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MerchantRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : MerchantRepository {

    override suspend fun addFood(food: Food): Result<Unit> {
        return try {
            supabaseClient.postgrest["foods"].insert(food)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFood(food: Food): Result<Unit> {
        return try {
            supabaseClient.postgrest["foods"].update(food) {
                filter {
                    eq("id", food.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMerchantMenu(storeId: String): Flow<List<Food>> = flow {
        val response = supabaseClient.postgrest["foods"]
            .select {
                filter {
                    eq("store_id", storeId)
                }
            }
            .decodeList<Food>()
        emit(response)
    }

    override suspend fun getStoreById(storeId: String): Store {
        return supabaseClient.postgrest["stores"]
            .select {
                filter {
                    eq("id", storeId)
                }
            }
            .decodeSingle<Store>()
    }

    override suspend fun getStoreByOwnerId(ownerId: String): Store? {
        return try {
            supabaseClient.postgrest["stores"]
                .select {
                    filter { eq("owner_id", ownerId) }
                }.decodeSingleOrNull<Store>()
        } catch (e: Exception) { null }
    }
}