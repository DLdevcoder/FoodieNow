package com.example.foodienow.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartItem(cartEntity: CartEntity)

    @Query("SELECT * FROM cart_items WHERE foodId = :foodId")
    fun getCartItemById(foodId: String): CartEntity?

    @Query("DELETE FROM cart_items WHERE foodId = :foodId")
    fun deleteCartItem(foodId: String)

    @Query("DELETE FROM cart_items")
    fun clearCart()

    @Query("SELECT storeId FROM cart_items LIMIT 1")
    fun getCurrentStoreId(): String?
}
