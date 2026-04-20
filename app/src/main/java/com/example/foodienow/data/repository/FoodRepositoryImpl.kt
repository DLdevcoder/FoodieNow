package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CustomerFoodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : CustomerFoodRepository {

    override fun getRecommendedFoods(): Flow<List<Food>> = flow {
        val response = supabaseClient.postgrest["foods"]
            .select()
            .decodeList<Food>()
        emit(response)
    }

    override fun searchFoods(query: String): Flow<List<Food>> = flow {
        val response = supabaseClient.postgrest["foods"]
            .select {
                filter {
                    ilike("name", "%$query%")
                }
            }
            .decodeList<Food>()
        emit(response)
    }
}