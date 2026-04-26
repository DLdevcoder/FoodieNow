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

    // 1. Lấy danh sách món ăn gợi ý (Trang chủ)
    override fun getRecommendedFoods(): Flow<List<Food>> = flow {
        val response = supabaseClient.postgrest["foods"]
            .select()
            .decodeList<Food>()
        emit(response)
    }

    // 2. Tìm kiếm món ăn theo tên
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

    // 3. Lấy chi tiết một món ăn (Dùng cho FoodDetailScreen)
    override suspend fun getFoodById(foodId: String): Food {
        return supabaseClient.postgrest["foods"]
            .select {
                filter {
                    eq("id", foodId)
                }
            }
            .decodeSingle<Food>()
    }

    // 4. Lấy danh sách món ăn của một cửa hàng cụ thể (Dành cho Store Detail)
    override suspend fun getFoodsByStoreId(storeId: String): List<Food> {
        return supabaseClient.postgrest["foods"]
            .select {
                filter {
                    eq("store_id", storeId) // Đảm bảo khớp với tên cột mới trong DB
                }
            }
            .decodeList<Food>()
    }

    // 5. Thêm món ăn mới (Dành cho Merchant)
    suspend fun addFood(food: Food, imageBytes: ByteArray?) {
        val finalImageUrl: String = if (imageBytes != null && imageBytes.isNotEmpty()) {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val bucket = supabaseClient.storage.from("food_images")

            bucket.upload(path = fileName, data = imageBytes)
            bucket.publicUrl(fileName)
        } else {
            "https://placeholder.com/food_default.png"
        }
        val foodToSave = food.copy(imageUrl = finalImageUrl)
        supabaseClient.postgrest["foods"].insert(foodToSave)
    }
}