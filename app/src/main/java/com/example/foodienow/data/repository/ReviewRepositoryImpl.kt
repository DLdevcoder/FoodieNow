package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Review
import com.example.foodienow.domain.model.ReviewUiModel
import com.example.foodienow.domain.repository.ReviewRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class ReviewWithUserResponse(
    val id: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profiles") val profiles: UserInfoResponse? = null
)
@Serializable
data class UserInfoResponse(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

class ReviewRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ReviewRepository {

    override suspend fun getReviewsByFoodId(foodId: String): List<ReviewUiModel> {
        val response = supabase.postgrest["reviews"]
            .select(columns = Columns.raw("id, rating, comment, created_at, profiles(full_name, avatar_url)")){
                filter { eq("food_id", foodId) }
            }.decodeList<ReviewWithUserResponse>()

        return response.map { item ->
            ReviewUiModel(
                id = item.id,
                userName = item.profiles?.fullName ?: "Người dùng",
                userAvatarUrl = item.profiles?.avatarUrl,
                rating = item.rating,
                comment = item.comment ?: "",
                date = item.createdAt?.substringBefore("T") ?: ""
            )
        }
    }

    override suspend fun getReviewByOrderAndFood(orderId: String, foodId: String): Review? {
        return try {
            supabase.postgrest["reviews"].select {
                filter {
                    eq("order_id", orderId)
                    eq("food_id", foodId)
                }
            }.decodeSingleOrNull<Review>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun submitReview(orderId: String, customerId: String, foodId: String, rating: Int, comment: String): Boolean {
        return try {
            @Serializable
            data class ReviewInsert(
                @SerialName("order_id") val orderId: String,
                @SerialName("customer_id") val customerId: String,
                @SerialName("food_id") val foodId: String,
                val rating: Int,
                val comment: String
            )

            supabase.postgrest["reviews"].insert(
                ReviewInsert(
                    orderId = orderId,
                    customerId = customerId,
                    foodId = foodId,
                    rating = rating,
                    comment = comment
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateReview(reviewId: String, rating: Int, comment: String): Boolean {
        return try {
            supabase.postgrest["reviews"].update(
                {
                    set("rating", rating)
                    set("comment", comment)
                }
            ) {
                filter { eq("id", reviewId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
