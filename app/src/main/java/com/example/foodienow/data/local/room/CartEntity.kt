package com.example.foodienow.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.foodienow.domain.model.Food

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val foodId: String,
    val storeId: String,
    val name: String,
    val description: String?,
    val price: Long,
    val imageUrl: String?,
    val quantity: Int
) {
    fun toFood(): Food {
        return Food(
            id = foodId,
            storeId = storeId,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl
        )
    }

    companion object {
        fun fromFood(food: Food, quantity: Int): CartEntity {
            return CartEntity(
                foodId = food.id,
                storeId = food.storeId,
                name = food.name,
                description = food.description,
                price = food.price,
                imageUrl = food.imageUrl,
                quantity = quantity
            )
        }
    }
}
