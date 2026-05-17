package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Category
import com.example.foodienow.domain.model.Food
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.MerchantRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
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

    override suspend fun updateFood(food: Food, imageBytes: ByteArray?): Result<Unit> {
        return try {
            var finalImageUrl = food.imageUrl

            if (imageBytes != null) {
                val fileName = "food_${System.currentTimeMillis()}.jpg"
                val bucket = supabaseClient.storage["food_images"]
                bucket.upload(fileName, imageBytes)

                val projectId = "ruyrncmsawymsrvsluae"
                finalImageUrl = "https://$projectId.supabase.co/storage/v1/object/public/food_images/$fileName"
            }

            supabaseClient.postgrest["foods"].update({
                set("name", food.name)
                set("price", food.price)
                set("description", food.description)
                set("image_url", finalImageUrl)
                set("is_available", food.isAvailable)
                set("category", food.categoryId)
            }) {
                filter {
                    eq("id", food.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
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

    override suspend fun updateStore(store: Store, imageBytes: ByteArray?): Result<Unit> {
        return try {
            var finalImageUrl = store.imageUrl

            if (imageBytes != null) {
                val fileName = "store_${System.currentTimeMillis()}.jpg"
                val bucket = supabaseClient.storage["store_images"]
                bucket.upload(fileName, imageBytes)

                val projectId = "ruyrncmsawymsrvsluae"
                finalImageUrl = "https://$projectId.supabase.co/storage/v1/object/public/store_images/$fileName"
            }

            supabaseClient.postgrest["stores"].update({
                set("name", store.name)
                set("address", store.address)
                set("image_url", finalImageUrl)
                set("opening_time", store.openingTime)
                set("closing_time", store.closingTime)
                set("is_active", store.isActive)
            }) {
                filter {
                    eq("id", store.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): List<Category> {
        return supabaseClient.postgrest["categories"]
            .select()
            .decodeList<Category>()
    }

    override suspend fun createCategory(name: String): Category {
        return supabaseClient.postgrest["categories"]
            .insert(Category(name = name)) { select() }
            .decodeSingle<Category>()
    }
}