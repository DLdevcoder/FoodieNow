package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.ReviewUiModel
import com.example.foodienow.domain.repository.ReviewRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns // Thêm dòng import này
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class ReviewWithUserResponse(
    val id: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String,
    val users: UserInfoResponse?
)

@Serializable
data class UserInfoResponse(
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

class ReviewRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ReviewRepository {

    override suspend fun getReviewsByFoodId(foodId: String): List<ReviewUiModel> {
        val response = supabase.postgrest["reviews"]
            // Sửa lại dòng select ở đây
            .select(columns = Columns.raw("*, users(full_name, avatar_url)")) {
                filter { eq("food_id", foodId) }
            }.decodeList<ReviewWithUserResponse>()

        return response.map { item ->
            ReviewUiModel(
                id = item.id,
                userName = item.users?.fullName ?: "Người dùng FoodieNow",
                userAvatarUrl = item.users?.avatarUrl,
                rating = item.rating,
                comment = item.comment ?: "",
                date = item.createdAt.substringBefore("T")
            )
        }
    }
    override suspend fun submitReview(foodId: String, userId: String, rating: Int, comment: String): Boolean {
        return try {
            @Serializable
            data class ReviewInsert(
                @SerialName("food_id") val foodId: String,
                @SerialName("user_id") val userId: String,
                val rating: Int,
                val comment: String
            )
            
            supabase.postgrest["reviews"].insert(
                ReviewInsert(foodId = foodId, userId = userId, rating = rating, comment = comment)
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}