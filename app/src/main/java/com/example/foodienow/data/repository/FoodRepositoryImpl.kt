package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.repository.CustomerFoodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
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

    suspend fun addFood(food: Food, imageBytes: ByteArray?) {
        val finalImageUrl: String = if (imageBytes != null && imageBytes.isNotEmpty()) {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val bucket = supabaseClient.storage.from("food_images")

            bucket.upload(path = fileName, data = imageBytes)
            bucket.publicUrl(fileName)
        } else {
            "https://www.citypng.com/public/uploads/preview/loading-load-icon-transparent-png-701751695033022vy5stltzj3.png"
        }
        val foodToSave = food.copy(imageUrl = finalImageUrl)
        supabaseClient.postgrest["foods"].insert(foodToSave)
    }
}