package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Food
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

    override fun getMerchantMenu(merchantId: String): Flow<List<Food>> = flow {
        val response = supabaseClient.postgrest["foods"]
            .select {
                filter {
                    eq("merchantId", merchantId)
                }
            }
            .decodeList<Food>()
        emit(response)
    }
}
